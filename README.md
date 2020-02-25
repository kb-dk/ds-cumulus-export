# ds-cumulus-export
Automated export from Cumulus

## Requirements
* Java 11
* Maven 3

## Premise

The purpose of this application is to extract metadata for a given image collection from the Cumulus
image application at the Royal Danish Library and produce XML, directly usable for indexing into
Solr. See [ds-solr](https://github.com/Det-Kongelige-Bibliotek/ds-solr) for Solr setup.

## Setup

`ds-cumulus-export` needs a running instance of
[ds-image-analysis](https://github.com/Det-Kongelige-Bibliotek/ds-image-analysis)

The exporter also requires a YAML-config stating Cumulus server, userid, password etc.

Either take the pre-filled configuration from the Digisam Confluence at
 `Teknisk dokumentation/Confs/Backend/ds-cumulus-export.yml` or
 copy `src/main/conf/ds-cumulus-export.yml` to `user.home` and fill in the missing values.
 In the latter case, you have to contact a KB-developer on the Digisam-project for the credentials.
 Remember to check that the `&dHashService`-part of the YAML refers to the right port.

The exporter also requires a mapping file, which describes which Cumulus fields should
be converted to which Solr fields: Copy `src/main/conf/ds-cumulus-export-default-mapping.yml`
to `user.home`. This file does not need to be adjusted.

(the location of the config file will be made flexible at a later point)

This project requires [Cumulus JAVA SDK](https://sbprojects.statsbiblioteket.dk/display/AIM/Cumulus+Java+SDK).
See the section "Installing cumulus API" at the bottom of this README.

## Build & deploy

When the config files are copied and the Cumulus API is installed,
`ds-cumulus-export` can be build with the standard
```
mvn package
```

After build has been completed, there should be a tar-archive intended for deployment in the `target`-folder.
 In order to use the application, the archive must be unpacked:
```
tar -xf target/cumulus-export-0.1-SNAPSHOT-distribution.tar.gz -C target/
```

## Run

After building & deploying, an export can be activated with
```
target/cumulus-export-0.1-SNAPSHOT/bin/cumulus-export.sh
```
which will produce an XML-file. If using the configuration from Confluence, the
file will be named `indexThisInSolr.xml` and be in the current folder. This file is intended for
indexing into Solr. See the README for [ds-solr](https://github.com/Det-Kongelige-Bibliotek/ds-solr)
for details.

The log-file is located in `user.home/logs/ds-cumulus-export.log`

## Extract statistics

For debugging of metadata and future changes to the mapping of fields, the project has a tool for
 providing statistics of Cumulus records: Extract the tar, as described above, and run
```
target/cumulus-export-0.1-SNAPSHOT/bin/cumulus-stats.sh > stats.log
```
This will produce the file `stats.log`. Note that it will update the file for every 1000 records,
so when the full run has finished, only the latest statistics-dump (the one at the end of the
file) should be used.

## Developer notes

The parent-pom for this project has [forbiddenapis](https://github.com/policeman-tools/forbidden-apis),
enabled, which checks for Java methods that are unsafe to use. One very common mistake is to use a
Locale-dependent API without specifying a Locale, e.g. writing `String.format("%.1f", 0.999)`
instead of `String.format(Locale.ENGLISH, "%.1f", 1.234);`: The output from the non-Locale
version will depend on the platform it is running on.

It would be nice, as a developer, to be able to run the exporter with
```
mvn exec:java
```
but unfortunately it does not (yet) work due to the Cumulus API not being a standard Maven dependency.

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

