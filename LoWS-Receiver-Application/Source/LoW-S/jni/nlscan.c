/**
 * 802.11 Scanner using Netlink80211
 *
 * 
 * Sept, 2014 * Sven Zehl * svenzehl@gmail.com
 */

#include <errno.h>
#include <stdio.h>
#include <string.h>
#include <net/if.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdbool.h>
#include <ctype.h>

#include <netlink/genl/family.h>
#include <netlink/genl/ctrl.h>
#include <netlink/genl/genl.h>
#include <netlink/msg.h>
#include <netlink/attr.h>
#include <netlink/handlers.h>
#include <linux/genetlink.h>
#include <errno.h>

#include "nl80211.h"
#include "nlscan.h"
//#include "nlscan_jni.h"

#include <android/log.h>

//#define NLSCAN_PERMANENT //If enabled, the created binary tries to set the wlan chip in permanent scanning mode
						//variable freq must be set to the desired frequency.
//#define ROOT_ENABLED

#ifndef ANDROID
#define ANDROID
#endif

#ifdef ANDROID
#define printfandroid(...) __android_log_print(ANDROID_LOG_DEBUG, "nlscanner", __VA_ARGS__);
#define nl_handle nl_sock
#endif

#ifdef ANDROID
static int android_genl_ctrl_resolve(struct nl_handle *handle,
				     const char *name)
{
	/*
	 * Android ICS has very minimal genl_ctrl_resolve() implementation, so
	 * need to work around that.
	 */
	struct nl_cache *cache = NULL;
	struct genl_family *nl80211 = NULL;
	int id = -1;

	if (genl_ctrl_alloc_cache(handle, &cache) < 0) {
		printfandroid("nl80211: Failed to allocate generic netlink cache\n");
		goto fail;
	}

	nl80211 = genl_ctrl_search_by_name(cache, name);
	if (nl80211 == NULL)
		goto fail;

	id = genl_family_get_id(nl80211);

fail:
	if (nl80211)
		genl_family_put(nl80211);
	if (cache)
		nl_cache_free(cache);

	return id;
}
//#define genl_ctrl_resolve android_genl_ctrl_resolve
#endif /* ANDROID */




//Global Data
struct nl80211_state nlstate;
signed long long devidx = 0;

void print_ssid_escaped(const uint8_t len, const uint8_t *data) {
	int i;

	for (i = 0; i < len; i++) {
		if (isprint(data[i]) && data[i] != ' ' && data[i] != '\\')
			printf("%c", data[i]);
		else if (data[i] == ' ' && (i != 0 && i != len - 1))
			printf(" ");
		else
			printf("\\x%.2x", data[i]);
	}
}

static void print_ssid(const uint8_t type, uint8_t len, const uint8_t *data) {
	printf("\tSSID: ");
	print_ssid_escaped(len, data);
	printf("\n");
}

static int error_handler(struct sockaddr_nl *nla, struct nlmsgerr *err,
		void *arg) {
	int *ret = arg;
	*ret = err->error;
	printfandroid("->CFG80211 returns: error: No:%d, %s\n", err->error,
			strerror((-1) * err->error));
	return NL_STOP;
}

static int finish_handler(struct nl_msg *msg, void *arg) {
	int *ret = arg;
	*ret = 0;
	printfandroid("Finish handler called\n");
	return NL_SKIP;
}

static int ack_handler(struct nl_msg *msg, void *arg) {
	int *ret = arg;
	*ret = 0;
	printfandroid("->CFG80211 returns: Request acknowledged\n");
	return NL_STOP;
}

static int family_handler(struct nl_msg *msg, void *arg) {
	struct handler_args *grp = arg;
	struct nlattr *tb[CTRL_ATTR_MAX + 1];
	struct genlmsghdr *gnlh = nlmsg_data(nlmsg_hdr(msg));
	struct nlattr *mcgrp;
	int rem_mcgrp;

	nla_parse(tb, CTRL_ATTR_MAX, genlmsg_attrdata(gnlh, 0),
			genlmsg_attrlen(gnlh, 0), NULL);

	if (!tb[CTRL_ATTR_MCAST_GROUPS])
		return NL_SKIP;

	nla_for_each_nested(mcgrp, tb[CTRL_ATTR_MCAST_GROUPS], rem_mcgrp) {
		struct nlattr *tb_mcgrp[CTRL_ATTR_MCAST_GRP_MAX + 1];

		nla_parse(tb_mcgrp, CTRL_ATTR_MCAST_GRP_MAX, nla_data(mcgrp),
				nla_len(mcgrp), NULL);

		if (!tb_mcgrp[CTRL_ATTR_MCAST_GRP_NAME]
				|| !tb_mcgrp[CTRL_ATTR_MCAST_GRP_ID])
			continue;
		if (strncmp(nla_data(tb_mcgrp[CTRL_ATTR_MCAST_GRP_NAME]), grp->group,
				nla_len(tb_mcgrp[CTRL_ATTR_MCAST_GRP_NAME])))
			continue;
		grp->id = nla_get_u32(tb_mcgrp[CTRL_ATTR_MCAST_GRP_ID]);
		break;
	}
}

static int no_seq_check(struct nl_msg *msg, void *arg) {
	return NL_OK;
}

static int wait_event(struct nl_msg *msg, void *arg) {
	struct wait_event *wait = arg;
	struct genlmsghdr *gnlh = nlmsg_data(nlmsg_hdr(msg));
	int i;

	for (i = 0; i < wait->n_cmds; i++) {
		if (gnlh->cmd == wait->cmds[i]) {
			wait->cmd = gnlh->cmd;
		}
	}

	return NL_SKIP;
}

void mac_addr_n2a(char *mac_addr, unsigned char *arg) {
	int i, l;

	l = 0;
	for (i = 0; i < ETH_ALEN; i++) {
		if (i == 0) {
			sprintf(mac_addr + l, "%02x", arg[i]);
			l += 2;
		} else {
			sprintf(mac_addr + l, ":%02x", arg[i]);
			l += 3;
		}
	}
}

void print_ies(unsigned char *ie, int ielen, bool unknown,
		enum print_ie_type ptype) {
	while (ielen >= 2 && ielen >= ie[1]) {
		if (ie[0] == 0/* ssid */) {
			print_ssid(ie[0], ie[1], ie + 2);
		} 

		int i;

		printf("\tIE (%d, len=%d) IE data (hex):", ie[0], ie[1]);
		for (i = 0; i < (ie[1]+2); i++)
			printf(" %.2x", ie[0 + i]);
		printf("\n");

		ielen -= ie[1] + 2;
		ie += ie[1] + 2;
	}
}

static int print_bss_handler(struct nl_msg *msg, void *arg) {
	printf("**********************************************\n");
	printfandroid("**********************************************\n");
	struct nlattr *tb[NL80211_ATTR_MAX + 1];
	struct genlmsghdr *gnlh = nlmsg_data(nlmsg_hdr(msg));
	struct nlattr *bss[NL80211_BSS_MAX + 1];
	char mac_addr[20], dev[20];
	static struct nla_policy bss_policy[NL80211_BSS_MAX + 1] = {
			[NL80211_BSS_TSF] = { .type = NLA_U64 }, [NL80211_BSS_FREQUENCY] = {
					.type = NLA_U32 }, [NL80211_BSS_BSSID] = { },
			[NL80211_BSS_BEACON_INTERVAL] = { .type = NLA_U16 },
			[NL80211_BSS_CAPABILITY] = { .type = NLA_U16 },
			[NL80211_BSS_INFORMATION_ELEMENTS] = { }, [NL80211_BSS_SIGNAL_MBM
					] = { .type = NLA_U32 }, [NL80211_BSS_SIGNAL_UNSPEC] = {
					.type = NLA_U8 },
			[NL80211_BSS_STATUS] = { .type = NLA_U32 }, [NL80211_BSS_SEEN_MS_AGO
					] = { .type = NLA_U32 }, [NL80211_BSS_BEACON_IES] = { }, };
	struct scan_params *params = arg;
	int show = params->show_both_ie_sets ? 2 : 1;
	bool is_dmg = false;

	nla_parse(tb, NL80211_ATTR_MAX, genlmsg_attrdata(gnlh, 0),
			genlmsg_attrlen(gnlh, 0), NULL);

	if (!tb[NL80211_ATTR_BSS]) {
		printfandroid("bss info missing!\n");
		return NL_SKIP;
	}
	if (nla_parse_nested(bss, NL80211_BSS_MAX, tb[NL80211_ATTR_BSS],
			bss_policy)) {
		printfandroid("failed to parse nested attributes!\n");
		return NL_SKIP;
	}

	if (!bss[NL80211_BSS_BSSID])
		return NL_SKIP;

	mac_addr_n2a(mac_addr, nla_data(bss[NL80211_BSS_BSSID]));
	printf("BSS %s\n", mac_addr);
	printfandroid("BSS %s\n", mac_addr);
	/*
	 if (tb[NL80211_ATTR_IFINDEX]) {
	 if_indextoname(nla_get_u32(tb[NL80211_ATTR_IFINDEX]), dev);
	 printf("(on %s)", dev);
	 }

	 if (bss[NL80211_BSS_STATUS]) {
	 switch (nla_get_u32(bss[NL80211_BSS_STATUS])) {
	 case NL80211_BSS_STATUS_AUTHENTICATED:
	 printf(" -- authenticated");
	 break;
	 case NL80211_BSS_STATUS_ASSOCIATED:
	 printf(" -- associated");
	 break;
	 case NL80211_BSS_STATUS_IBSS_JOINED:
	 printf(" -- joined");
	 break;
	 default:
	 printf(" -- unknown status: %d",
	 nla_get_u32(bss[NL80211_BSS_STATUS]));
	 break;
	 }
	 }
	 printf("\n");

	 if (bss[NL80211_BSS_TSF]) {
	 unsigned long long tsf;
	 tsf = (unsigned long long)nla_get_u64(bss[NL80211_BSS_TSF]);
	 printf("\tTSF: %llu usec (%llud, %.2lld:%.2llu:%.2llu)\n",
	 tsf, tsf/1000/1000/60/60/24, (tsf/1000/1000/60/60) % 24,
	 (tsf/1000/1000/60) % 60, (tsf/1000/1000) % 60);
	 }
	*/
	 if (bss[NL80211_BSS_FREQUENCY]) {
	 int freq = nla_get_u32(bss[NL80211_BSS_FREQUENCY]);
	 printf("\tfreq: %d\n", freq);
	 printfandroid("\tfreq: %d\n", freq);
	 if (freq > 45000)
	 is_dmg = true;
	 }
	 
	 if (bss[NL80211_BSS_BEACON_INTERVAL])
	 printf("\tbeacon interval: %d\n",
	 nla_get_u16(bss[NL80211_BSS_BEACON_INTERVAL]));
	 printfandroid("\tbeacon interval: %d\n",
	 nla_get_u16(bss[NL80211_BSS_BEACON_INTERVAL]));
	 /*
	 if (bss[NL80211_BSS_CAPABILITY]) {
	 __u16 capa = nla_get_u16(bss[NL80211_BSS_CAPABILITY]);
	 printf("\tcapability:");
	 if (is_dmg)
	 print_capa_dmg(capa);
	 else
	 print_capa_non_dmg(capa);
	 printf(" (0x%.4x)\n", capa);
	 }
	 */
	if (bss[NL80211_BSS_SIGNAL_MBM]) {
		int s = nla_get_u32(bss[NL80211_BSS_SIGNAL_MBM]);
		printf("\tsignal (dBm): %d.%.2d\n", s / 100, s % 100);
		printfandroid("\tsignal (dBm): %d.%.2d\n", s / 100, s % 100);
	}
	/*
	if (bss[NL80211_BSS_SIGNAL_UNSPEC]) {
		unsigned char s = nla_get_u8(bss[NL80211_BSS_SIGNAL_UNSPEC]);
		printf("\tsignal: %d/100\n", s);
	}
	*/
	if (bss[NL80211_BSS_SEEN_MS_AGO]) {
		int age = nla_get_u32(bss[NL80211_BSS_SEEN_MS_AGO]);
		printf("\tlast seen (ms ago): %d\n", age);
		printfandroid("\tlast seen (ms ago): %d\n", age);
	}

	if (bss[NL80211_BSS_INFORMATION_ELEMENTS] && show--) {
		if (bss[NL80211_BSS_BEACON_IES])
			printf("\tInformation elements from Probe Response "
					"frame:\n");
			printfandroid("\tInformation elements from Probe Response "
					"frame:\n");
		print_ies(nla_data(bss[NL80211_BSS_INFORMATION_ELEMENTS]),
				nla_len(bss[NL80211_BSS_INFORMATION_ELEMENTS]), params->unknown,
				params->type);
	}
	if (bss[NL80211_BSS_BEACON_IES] && show--) {
		printf("\tInformation elements from Beacon frame:\n");
		printfandroid("\tInformation elements from Beacon frame:\n");
		print_ies(nla_data(bss[NL80211_BSS_BEACON_IES]),
				nla_len(bss[NL80211_BSS_BEACON_IES]), params->unknown,
				params->type);
	}

	return NL_SKIP;
}

static int nl80211_init(struct nl80211_state *state) {
	int err;
	printfandroid("nl80211_init() started...\n");
	state->nl_sock = nl_socket_alloc();
	if (!state->nl_sock) {
		printfandroid("Failed to allocate netlink socket.\n");
		return -ENOMEM;
	}

	nl_socket_set_buffer_size(state->nl_sock, 8192, 8192);

	if (genl_connect(state->nl_sock)) {
		printfandroid("Failed to connect to generic netlink.\n");
		err = -ENOLINK;
		goto out_handle_destroy;
	}

	state->nl80211_id = android_genl_ctrl_resolve(state->nl_sock, "nl80211");
	if (state->nl80211_id < 0) {
		printfandroid("nl80211 not found.\n");
		err = -ENOENT;
		goto out_handle_destroy;
	}

	return 0;

	out_handle_destroy: nl_socket_free(state->nl_sock);
	return err;
}


int main() {

	printfandroid("nlscan_start() called!\n");
	int err, i;
	char device[] = "wlan0";
	struct nl_msg *msg;
	struct nl_cb *cb;
	printfandroid("Starting nl80211_init()\n");
	err = nl80211_init(&nlstate);
	printfandroid("nl80211_init() closed with error code: %d\n",err);
	
	//Get the results of the scan
	printfandroid("Get the results from driver...\n");
	printfandroid("Set callback...\n");

	devidx = if_nametoindex(device);

	msg = nlmsg_alloc();
	cb = nl_cb_alloc(NL_CB_DEFAULT);
	struct nl_cb *s_cb = nl_cb_alloc(NL_CB_DEFAULT);
	static struct scan_params scan_params;

	memset(&scan_params, 0, sizeof(scan_params));

	//get the unknown information elements
	scan_params.unknown = true;
	//scan_params.show_both_ie_sets = true; //Hmm?
	scan_params.type = PRINT_SCAN;

	nl_cb_set(cb, NL_CB_VALID, NL_CB_CUSTOM, print_bss_handler, &scan_params);

	//nl_socket_set_cb(nlstate.nl_sock, s_cb);

	printfandroid("Send get scan results command...\n");
	printf("#");
	genlmsg_put(msg, 0, 0, nlstate.nl80211_id, 0, NLM_F_DUMP,
			NL80211_CMD_GET_SCAN, 0);

	NLA_PUT_U32(msg, NL80211_ATTR_IFINDEX, devidx);

	err = nl_send_auto_complete(nlstate.nl_sock, msg);
	nl_cb_err(cb, NL_CB_CUSTOM, error_handler, &err);
	nl_cb_set(cb, NL_CB_FINISH, NL_CB_CUSTOM, finish_handler, &err);
	nl_cb_set(cb, NL_CB_ACK, NL_CB_CUSTOM, ack_handler, &err);
	err = 1;
	while (err > 0) {
		nl_recvmsgs(nlstate.nl_sock, cb);
		if (err != 0) {
			printfandroid("error:%d\n", err);
		}
	}
	nl_cb_put(cb);
	nlmsg_free(msg);

	printfandroid("end of program reached...\n");

	return 0;

	nla_put_failure: printfandroid("NLA_PUT_FAILURE\n");
	return -ENOBUFS;
}
