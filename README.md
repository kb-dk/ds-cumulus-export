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

This project requires [Cumulus JAVA SDK](https://sbprojects.statsbiblioteket.dk/display/AIM/Cumulus+Java+SDK).
See the section "Installing cumulus API" at the bottom of this README.
 
## Build & run

When the config files are copied and the Cumulus API is installed, 
`ds-cumulus-export` can be build & run with
```
mvn package
tar -xf target/cumulus-export-0.1-SNAPSHOT-distribution.tar.gz -C target/
target/cumulus-export-0.1-SNAPSHOT/bin/cumulus-export.sh
```

To extract statistics for the Cumulus fields, run
```
target/cumulus-export-0.1-SNAPSHOT/bin/cumulus-stats.sh > stats.log
```
It will produce the file `stats.log`. Note that it will update the file for every 1000 records,
so when the full run has finished, only the latest statistics-dump (the one at the end of the
file) should be used.

## Developer note

The parent-pom for this project has [forbiddenapis](https://github.com/policeman-tools/forbidden-apis),
enabled, which checks for Java methods that are unsafe to use. One very common mistake is to use a
Locale-dependent API without specifying a Locale, e.g. writing `String.format("%.1f", 0.999)`
instead of `String.format(Locale.ENGLISH, "%.1f", 1.234);`: The output from the non-Locale
version will depend on the platform it is running on.

## Installing Cumulus API

Install the  [Cumulus JAVA SDK](https://sbprojects.statsbiblioteket.dk/display/AIM/Cumulus+Java+SDK)
by doing the following and accepting the default paths when asked: 

```
wget https://www.attentionfiles.dk/files2go/Cumulus/10.1.3/CJCSDK_1013_Linux.zip
unzip CJCSDK_1013_Linux.zip
chmod 755 CJSDK1013.bin
sudo ./CJSDK1013.bin
sudo chmod -R 755 /usr/local/Cumulus_Java_SDK/

sudo sh -c "echo '/usr/local/Cumulus_Java_SDK/lib' > /etc/ld.so.conf.d/cumulus.conf"
sudo ldconfig
```

