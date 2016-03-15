
start = 0x215221  # hex literal, gives us a regular integer
end = 0x21527e
p = 0

for i in xrange(start, end + 1):
    print 'backgroundScannerSearchStrings['+str(p)+']=' + '\"'+format(i, 'x')+'\"'+';'+' // Code Red'
    p=p+1

start = 0x216221  # hex literal, gives us a regular integer
end = 0x21627e

for i in xrange(start, end + 1):
    print 'backgroundScannerSearchStrings['+str(p)+']=' + '\"'+format(i, 'x')+'\"'+';'+' // Code Brown'
    p=p+1
    
start = 0x214721  # hex literal, gives us a regular integer
end = 0x21477e

for i in xrange(start, end + 1):
    print 'backgroundScannerSearchStrings['+str(p)+']=' + '\"'+format(i, 'x')+'\"'+';'+' // Code Green'
    p=p+1
   
start = 0x214221  # hex literal, gives us a regular integer
end = 0x21427e

for i in xrange(start, end + 1):
    print 'backgroundScannerSearchStrings['+str(p)+']=' + '\"'+format(i, 'x')+'\"'+';'+' // Code Black'
    p=p+1
   
start = 0x215021  # hex literal, gives us a regular integer
end = 0x21507e

for i in xrange(start, end + 1):
    print 'backgroundScannerSearchStrings['+str(p)+']=' + '\"'+format(i, 'x')+'\"'+';'+' // Code Pink'
    p=p+1
  
start = 0x215721  # hex literal, gives us a regular integer
end = 0x21577e

for i in xrange(start, end + 1):
    print 'backgroundScannerSearchStrings['+str(p)+']=' + '\"'+format(i, 'x')+'\"'+';'+' // Code White'
    p=p+1

start = 0x214f21  # hex literal, gives us a regular integer
end = 0x214f7e

for i in xrange(start, end + 1):
    print 'backgroundScannerSearchStrings['+str(p)+']=' + '\"'+format(i, 'x')+'\"'+';'+' // Code Orange'
    p=p+1
    
start = 0x215921  # hex literal, gives us a regular integer
end = 0x21597e

for i in xrange(start, end + 1):
    print 'backgroundScannerSearchStrings['+str(p)+']=' + '\"'+format(i, 'x')+'\"'+';'+' // Code Yellow'
    p=p+1

print 'Overall Search Strings'
print str(p)







start = 0x215221  # hex literal, gives us a regular integer
end = 0x21527e
p = 0

for i in xrange(start, end + 1):
    print 'backgroundScannerDisplayStrings['+str(p)+']=\"CODE RED: Fire Emergency!\"; // Code Red'
    p=p+1

start = 0x216221  # hex literal, gives us a regular integer
end = 0x21627e

for i in xrange(start, end + 1):
    print 'backgroundScannerDisplayStrings['+str(p)+']=\"CODE BROWN: Severe Weather Emergency!\"; // Code Brown'
    p=p+1
    
start = 0x214721  # hex literal, gives us a regular integer
end = 0x21477e

for i in xrange(start, end + 1):
    print 'backgroundScannerDisplayStrings['+str(p)+']=\"CODE GREEN: Internal Disaster!\"; // Code Green'
    p=p+1
   
start = 0x214221  # hex literal, gives us a regular integer
end = 0x21427e

for i in xrange(start, end + 1):
    print 'backgroundScannerDisplayStrings['+str(p)+']=\"CODE BLACK: Bomb Threat!\"; // Code Black'
    p=p+1
   
start = 0x215021  # hex literal, gives us a regular integer
end = 0x21507e

for i in xrange(start, end + 1):
    print 'backgroundScannerDisplayStrings['+str(p)+']=\"CODE PINK: Child Abduction!\"; // Code Pink'
    p=p+1
  
start = 0x215721  # hex literal, gives us a regular integer
end = 0x21577e

for i in xrange(start, end + 1):
    print 'backgroundScannerDisplayStrings['+str(p)+']=\"CODE WHITE: Evacuation!\"; // Code White'
    p=p+1

start = 0x214f21  # hex literal, gives us a regular integer
end = 0x214f7e

for i in xrange(start, end + 1):
    print 'backgroundScannerDisplayStrings['+str(p)+']=\"CODE ORANGE: Aggressive Situation !\"; // Code Orange'
    p=p+1
    
start = 0x215921  # hex literal, gives us a regular integer
end = 0x21597e

for i in xrange(start, end + 1):
    print 'backgroundScannerDisplayStrings['+str(p)+']=\"CODE YELLOW: Medical Crisis !\"; // Code Yellow'
    p=p+1
    
print 'Overall Display Strings'
print str(p)

