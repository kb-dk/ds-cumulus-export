# ds-cumulus-export
Automated export from Cumulus

## Requirements
* Java 11
* Maven 3

## Setup

The exporter requires a YAML stating Cumulus server, userid, password etc.

Copy `src/test/resources/ds-cumulus-export.yml` to `user.home` and fill in the missing values.
Contact a KB-developer on the DigiSam-project for the credentials.

(the location of the config file will be made flexible at a later point)

## Build & run

This project requires both [Cumulus JAVA SDK](https://sbprojects.statsbiblioteket.dk/display/AIM/Cumulus+Java+SDK) 

```
wget https://www.attentionfiles.dk/files2go/Cumulus/10.1.3/CJCSDK_1013_Linux.zip
unzip CJCSDK_1013_Linux.zip
chmod 755 CJSDK1013.bin
sudo ./CJSDK1013.bin

sudo sh -c "echo '/usr/local/Cumulus_Java_SDK/lib' > /etc/ld.so.conf.d/cumulus.conf"
sudo ldconfig
```

and the [KB-Cumulus-API](https://github.com/Det-Kongelige-Bibliotek/KB-Cumulus-API) with
```
mvn install 
```
(this is expected to be changed later to a local install of Cumulus API)

test with the commands below (remember to privide `userid` and `password`)
```
mvn clean compile assembly:single
java -cp /usr/local/Cumulus_Java_SDK/CumulusJC.jar:target/kb-cumulus-api-0.1.5-jar-with-dependencies.jar dk.kb.cumulus.CumulusExtractor -scumulus-core-test-01.kb.dk -u<userid> -p<password> -cSamlingsbilleder -f001A_DSC-TEST_7203.tif
```

When this is done, we can run
```
mvn package
java -cp target/cumulus-export-0.1-SNAPSHOT.jar dk.kb.ds.cumulus.export.CumulusExport
```

## Status
Skeleton structure & code only.
