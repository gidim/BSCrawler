import cld
import os
import os
from fnmatch import fnmatch
import sys




#open the temp file java generated.
with open (sys.argv[1], "r") as myfile:
    data=myfile.read().replace('\n', '')

detectedLangName, detectedLangCode, isReliable, textBytesFound, details = cld.detect(str(data), pickSummaryLanguage=False, removeWeakMatches=False)
print '  lang: %s ,reliable: %i' % (detectedLangCode,isReliable)
