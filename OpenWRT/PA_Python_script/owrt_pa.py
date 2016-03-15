import urllib
import os
import time

def generateIE(name, number):
    numberint = int(number)
    numberhex = format(numberint, '04x')
    namehex = "".join("{:02x}".format(ord(c)) for c in name)
    iedata='aaaaaa8010'+ numberhex + namehex
    ielen=(len(iedata))/2
    ie='dd'+format(ielen, '02x')+iedata
    #ie='dd'+iedata
    #print ie
    #print '\n'
    return ie

while 1:
    print "\n Tafel an H2,H10,H23:"
    print "**********************************"
    page = urllib.urlopen('http://pruefungsamt.freitagsrunde.org/')
    room = 'nichts'
    number = 'nichts'
    h02 = '0'
    for line in page:
        if ('"room"' in line):
            room = line.strip()[19:]
            room = room.strip('</span>');
            #print room
        if ('"number"' in line) and not ('placeholder' in line):
            number = line.strip()[21:]
            number = number.strip('</span>');
            #print number
        if (room == 'H 02') and (number != 'nichts'):
            #if(number==h02):
            #    h02 = h02 + '*';
            #else:
            h02=number;
            room = 'nichts'
            number = 'nichts'
            print "H02: \t" + h02
        if (room == 'H 10') and (number != 'nichts'):
            h10=number
            room = 'nichts'
            number = 'nichts'
            print "H10: \t" + h10
        if (room == 'H 19') and (number != 'nichts'):
            h19=number
            room = 'nichts'
            number = 'nichts'
            print "H19: \t" + h19
        if (room == 'H 23') and (number != 'nichts'):
            h23=number
            room = 'nichts'
            number = 'nichts'
            print "H23: \t" + h23
        if (room == 'H 25') and (number != 'nichts'):
            h25=number
            room = 'nichts'
            number = 'nichts'
            print "H25: \t" + h25
    page.close()
    
    ie1 = generateIE("H02", h02)
    ie2 = generateIE("H10", h10)
    ie3 = generateIE("H19", h19)
    ie4 = generateIE("H23", h23)
    ie5 = generateIE("H25", h25)
    #h02int = int(h02)
    #h02hex = format(h02int, '06x')
    #ie1data='d00ea48010'+ '483032' + '00' + h02hex
    #ie1len=(len(ie1data))/2
    #ie1='dd'+format(ie1len, '02x')+ie1data
    #print ie1
    
    
    
    
    print "\n Tafel an H13:"
    print "**********************************"
    
    page = urllib.urlopen('http://pruefungsamt.freitagsrunde.org/13')
    room = 'nichts'
    number = 'nichts'
    for line in page:
        if ('"room"' in line):
            room = line.strip()[19:]
            room = room.strip('</span>');
            #print room
        if ('"number"' in line) and not ('placeholder' in line):
            number = line.strip()[21:]
            number = number.strip('</span>');
            #print number
        if (room == 'Schalter 1/2') and (number != 'nichts'):
            schalter12=number
            room = 'nichts'
            number = 'nichts'
            print "Schalter 1/2: \t\t" + schalter12
        if (room == 'Schalter 3/4') and (number != 'nichts'):
            schalter34=number
            room = 'nichts'
            number = 'nichts'
            print "Schalter 3/4: \t\t" + schalter34
        if (room == 'Schalter 5/6') and (number != 'nichts'):
            schalter56=number
            room = 'nichts'
            number = 'nichts'
            print "Schalter 5/6: \t\t" + schalter56
        if (room == 'Schalter 7/8/9') and (number != 'nichts'):
            schalter789=number
            room = 'nichts'
            number = 'nichts'
            print "Schalter 7/8/9: \t" + schalter789
        if (room == 'Schalter 10/11') and (number != 'nichts'):
            schalter1011=number
            room = 'nichts'
            number = 'nichts'
            print "Schalter 10/11: \t" + schalter1011
    page.close()
    ie6 = generateIE("H13, S1/2", schalter12)
    ie7 = generateIE("H13, S3/4", schalter34)
    ie8 = generateIE("H13, S5/6", schalter56)
    ie9 = generateIE("H13, S7/8/9", schalter789)
    ie10 = generateIE("H13, S10/11", schalter1011)
    all_ie = ie1+ie2+ie3+ie4+ie5+ie6+ie7+ie8+ie9+ie10
    hostapd_string= 'hostapd_cli add_ie ' + all_ie
    print hostapd_string
    os.system(hostapd_string)
    time.sleep(5)
