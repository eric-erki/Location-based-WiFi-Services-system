# The Location-based WiFi Services system: A Complete Open Source Solution for Wi-Fi Beacon Stuffing Based Location-based Services

## 0. What is LoWS?
Omnipresent Wi-Fi access points (APs) periodically
broadcast beacon frames to inform potential stations (STAs)
about their existence. Beacon frames can be extended by adding
additional information (so-called beacon stuffing) making it pos-
sible to deliver this information to mobile devices (smart-phones,
tablets, etc. equipped with 802.11 interfaces) without the need for
their association with the local infrastructure. This feature can be
used to support location-based information services (LBS) related
for e.g. to advertising local opportunities, temporary obstacles
and traffic disturbances or emergency notifications.
The Location-based WiFi Services system is the technical solutions for LBS
support of using 802.11 beacon stuffing. 
LoWS supports an original, highly efficient way to encode the delivered
information. The LoWS system can be integrated in an existent
802.11 infrastructure while the receiver application can be
installed on commercial off-the-shelf Android devices. The LoWS
system is modular with well-defined interfaces making it to an
attractive tool for modifications, changes and improvements.

## 1. File Description
### 1.1 LoWS-Receiver-Application

* Here the Android source code is stored, the complete
 Eclipse projects can be found in the subfolder
 "Source", the javadoc for the Android source can be 
 found in the subfolder "javadoc".
* In the Source subfolder the folder "release_apk"
 the apks which can be used to install the LoWS-Receiver-Application and the broadcastreceiver example are  stored.
* The "Backups" folder contains the sources as .zip 
 file.
* The "LoW-S" folder contains the eclipse project for
 the LoWS Receiver Application including the complete
 source code.
* The "lowsbroadcast" folder contains the eclipse pro-
 ject for the lowsbroadcast receiver example which 
 shows how to receive LoWS messages within an external application.

### 1.2 LoWS Control / LoWS-Control-DB

* This is the LoWS Control Program prototype, realized
 using PHP and MySQL, the corresponding MySQL database
 can be initialized by importing the file "create-lows-
 control-db.sql" in the subfolder "lows-control-db".
* An apache with PHP plus an MySQL server is needed.
* It is also useful to run an DHCP server on the same 
 machine but not necessary, for our experiments we used
 the address space 192.168.68.0/24 while the LoWS-Control
 used 192.168.68.1
### 1.3 Local Codebookserver
* This is the local codebookserver implementation,
 also realized using PHP. For our experiments we
 ran the codebookserver on the same server as the 
 LoWS-Control, therefore the access from the codebookserver to the LoWS-Control-db worked locally, if the local codebook server should run separately,
 the connection to the LoWS-Control-db has to be
 adjusted.
#### 1.3 Global Codebook Address Server
* This folder contains the Global Codebook Address Server
 also implemented using PHP, we ran this code on
 a separate machine using apache with PHP and a MySQL
 server. The Global Codebook Address Server DB can be installed by importing
 the file "gcbas-database.sql" from the MySQL server.
 The access to the database requires login credentials
 these have to be modified within the "address-handler.php" file. 
### 1.4 OpenWRT Files
* This folder includes all source code which was used
 on the OpenWRT access points. The subfolder "hostapd-add-ie/openwrt" contains the patch for enabling the 
 additonal setting of IEEE 802.11 IEs within hostpad via 
 the hostapd_cli daemon. The folder "hostapd-add-ie/hostapd-2.2" contains the full hostapd source with
 enabled IEEE 802.11 IE adding support via hostapd_cli
 which can be used on a standard x86 machine.
* The subfolder "beaconspammer" includes the beaconspammer" source code with a Makefile for OpenWRT. The paths
 in the Makefile have to adjusted to allow crosscompiling.
* The subfolder "mac80211-radiotap-rates-set" includes the
 mac80211 patch to enable beaconspammer the access to 
 the IEEE 802.11 rates via the radiotap header.
* The subfolder PA_Python_script contains a python script
 which accesses the waiting ticket numbers from the 
 TU Examination Office via the Freitagsrunde website
 The script parses the website and uses the modified 
 hostapd_cli to send WTN LoWS messages including the
 current waiting ticket numbers from the TU examination
 office.
### 1.5 Prototype Virtual Machines
* Within this folder, all the virtual machine images are
 used within our prototypical setup are stored. All of 
 them use a bridged Network Interface. During our experiments this physical bridged network interface used the fixed IP 192.168.68.111 and was connected via a switch 
 with the OpenWRT AP and the Cisco lightweight AP.
* Therefore two machines can be found there. All of them
 can be accessed via the Webmin Webinterface by just 
 typing the URL of the machine inside a browser. More-
 over, all of them have the standard root accout with
 the credentials: 
	* user: root
	* pass: toor
* The LoWS-Control VM includes the LoWS-Control Program,
 the LoWS-Control-DB and the local Codebookserver. 
 Besides, a MySQL Server, an Apache with PHP and a
 DHCP Server are running. IP: 192.168.68.1 fixed
* The global Codebook Address Server VM includes the global Codebook address server and the global Codebook Server Database. IP: 192.168.68.3 (DHCP)
*The AIR-CTVM-K9-8-0-100-0p VM houses the WLC of the
 Cisco lightweight AP. Due to its closed source the vWLC is not provided, if you want to use 
 Cisco lightweight APs within the LoWS system, please use your WLC or vWLC you purchased together with your lightweight AP from Cisco. For experiments, please contact Cisco to get a 
evaluation Version of their currently available vWLC. 

## 9. Contact
* Sven Zehl, TU-Berlin, zehl@tkn
* Niels Karowski, TU-Berlin, karowski@tkn
* Anatolij Zubow, TU-Berlin, zubow@tkn
* Adam Wolisz, TU-Berlin, wolisz@tkn
* tkn = tkn.tu-berlin.de
