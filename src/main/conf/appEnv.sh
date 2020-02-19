# File to be sourced in to application runner
# Allow for customisations here (allows application runner to be effectively read-only)

MAIN_CLASS=dk.kb.ds.cumulus.export.CumulusExport
STATS_CLASS=dk.kb.ds.cumulus.export.CumulusStats
APP_CONFIG=ds-cumulus-export.yaml

#Optional parameter to override default JAVA_OPTS
JAVA_OPTS="-Xmx512m"

#Optional parameter to override default classpath (lib folder)
# Normally the Cumuls SDK is /usr/local/Cumulus_Java_SDK/CumulusJC.jar but the installation folder
# can be specified during installation of the Cumul API, so we aalso sk ldconfig where it is.
# Furthermore we att the $HOME-variant for installations where there is no root access.
CLASS_PATH_OVERRIDE="$(ldconfig -p | grep -m 1 -o '/[^ ]*Cumulus_Java_SDK/')CumulusJC.jar:/usr/local/Cumulus_Java_SDK/CumulusJC.jar:${HOME}/Cumulus-SDK/CumulusJC.jar:$SCRIPT_DIR/../lib/*"


