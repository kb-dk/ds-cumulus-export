# File to be sourced in to application runner
# Allow for customisations here (allows application runner to be effectively read-only)

MAIN_CLASS=dk.kb.ds.cumulus.export.CumulusExport
STATS_CLASS=dk.kb.ds.cumulus.export.CumulusStats
APP_CONFIG=ds-cumulus-export.yaml

#Optional parameter to override default JAVA_OPTS
JAVA_OPTS="-Xmx512m"

# Normally the Cumuls SDK is /usr/local/Cumulus_Java_SDK/CumulusJC.jar but the installation folder
# can be specified during installation of the Cumul API, so we also ask ldconfig where it is.
# Furthermore we use the $HOME-variant for installations where there is no root access.

for CUMULUS_JAR in \
  "$(ldconfig -p | grep -m 1 -o '/[^ ]*Cumulus_Java_SDK/')CumulusJC.jar" \
  "/usr/local/Cumulus_Java_SDK/CumulusJC.jar" \
  "${HOME}/Cumulus-SDK/CumulusJC.jar"
do
  if [[ -s "$CUMULUS_JAR" ]]; then
    break
  fi
done
if [[ ! -s "$CUMULUS_JAR" ]]; then
  >&2 echo "Error: Unable to locate CumulusJC.jar - is the Cumulud SDK installed on the machine?"
  exit 2
fi
#Optional parameter to override default classpath (lib folder)
CLASS_PATH_OVERRIDE="$CUMULUS_JAR:$SCRIPT_DIR/../lib/*"
