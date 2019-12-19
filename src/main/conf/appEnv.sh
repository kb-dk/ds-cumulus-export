# File to be sourced in to application runner
# Allow for customisations here (allows application runner to be effectively read-only)

MAIN_CLASS=dk.kb.ds.cumulus.export.CumulusExport
STATS_CLASS=dk.kb.ds.cumulus.export.CumulusStats
APP_CONFIG=ds-cumulus-export.yaml

#Optional parameter to override default JAVA_OPTS
JAVA_OPTS="-Xmx512m"

#Optional parameter to override default classpath (lib folder)
CLASS_PATH_OVERRIDE="/usr/local/Cumulus_Java_SDK/CumulusJC.jar:$SCRIPT_DIR/../lib/*"


