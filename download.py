import os
import urllib.request
from multiprocessing.dummy import Pool as ThreadPool 

os.chdir("C:\\Users\\Benjamin LOISON\\Desktop\\DocSolus\\Enonces\\")

def replaceAllDotsWithoutLast(str):
    index = -1
    for i in range(len(str) - 1, -1, -1):
        if str[i] == '.':
            index = i
            break
    if index != -1:
        str = str[:index].replace('.', '_') + str[index:]
    return str

f = open("urls.txt")
urls = f.readlines()
f.close()

os.chdir("PDF")

urlsLen = len(urls)
failsCounter = 0
i = 0

def workOn(url):
    global failsCounter, i
    url = url.replace("\n", "")
    i += 1
    print(i)
    try:
        with urllib.request.urlopen("http://doc-solus.fr//prepa/sci/adc/pdf/enonces.pdf/" + url[-4:] + "/" + url + ".enonce.pdf") as response, open(replaceAllDotsWithoutLast(url).replace("/", "_") + ".pdf", 'wb') as outFile:
            outFile.write(response.read())
    except:
        failsCounter += 1
        print("Failed: " + str(failsCounter))

        
pool = ThreadPool(12) 
pool.map(workOn, urls)

pool.close() 
pool.join() 
        
print("Total fails: " + str(failsCounter))