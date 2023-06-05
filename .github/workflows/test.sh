echo "BUILDING IIOP CLIENT"

mvn clean install

IIOP_CLIENT_VERSION=`mvn help:evaluate -Dexpression=project.version -q -DforceStdout`

echo "CLONING WILDFLY"

git clone https://github.com/wildfly/wildfly

cd wildfly

echo "SWITCHING IIOP CLIENT VERSION IN WILDFLY TO ${IIOP_CLIENT_VERSION}"
mvn versions:set-property -Dproperty=version.org.jboss.iiop-client -DnewVersion=${IIOP_CLIENT_VERSION}

echo "BUILDING WILDFLY"
mvn clean install -DskipTests=true

cd testsuite/integration/iiop

echo "RUNNING IIOP INTEGRATION TEST"
mvn clean test
