# "arn:aws:sts::213207498725:assumed-role/AWSReservedSSO_Dev_Acquring_Admin_8a00d814b5183bdd/a-albert.oclarit@voyagerinnovation.com
ROLE=$(aws sts get-caller-identity | jq '.Arn' |   xargs -n1 echo)
IFS=':'
read -a PARTS <<< "$ROLE"
echo ${PARTS[5]}

EXPECTATION="arn:aws:iam::149461571144:role/test-ppe-file-based-parsers"
read -a PARTS <<< "$EXPECTATION"
echo ${PARTS[5]}