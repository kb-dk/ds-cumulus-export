# ds-cumulus-export
Automated export from Cumulus

## Requirements
* Java 11
* Maven 3

## Setup

The exporter requires a YAML stating Cumulus server, userid, password etc.

Copy `src/test/resources/ds-cumulus-export.yml` to `user.home` and fill in the missing values.
Contact a KB-developer on the DigiSam-project for the credentials.

Also copy `src/test/resources/ds-cumulus-export-default-mapping.yml` to `user.home`.
This file does not need to be adjusted.

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
java -cp /usr/local/Cumulus_Java_SDK/CumulusJC.jar:target/cumulus-export-0.1-SNAPSHOT-jar-with-dependencies.jar dk.kb.ds.cumulus.export.CumulusExport
```

To extract statistics for the Cumulus fields, run
```
java -cp /usr/local/Cumulus_Java_SDK/CumulusJC.jar:target/cumulus-export-0.1-SNAPSHOT-jar-with-dependencies.jar dk.kb.ds.cumulus.export.CumulusStats
```
It will produce the file `stats.log`. Note that it will update the file for every 1000 records, so when the full run has finished, only the latest statistics-dump (the one at the end of the file) should be used.

## Developer note

The parent-pom for this project offers [forbiddenapis](https://github.com/policeman-tools/forbidden-apis),
which checks for Java methods that are unsafe to use. One very common mistake is to use a
Locale-dependent API without specifying a Locale, e.g. writing `String.format("%.1f", 0.999)`
instead of `String.format(Locale.ENGLISH, "%.1f", 1.234);`: The output from the non-Locale
version will depend on the platform it is running on.

To check for forbidden APIs, run `mvn clean package forbiddenapis:check`.

## Status
Skeleton structure & code only.
