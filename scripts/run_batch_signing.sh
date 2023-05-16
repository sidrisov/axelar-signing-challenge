#!/bin/sh

echo "1) Generating Keys:"
curl -X POST http://localhost:8081/api/ingestor/keys
echo "\n\n2) Generating Records:"
curl -X POST http://localhost:8082/api/ingestor/records
echo "\n\n3) Initiating Batch Signing:"
curl -X POST http://localhost:8083/api/signing/batch

echo "\n\n4) Checking Stats (every 5 seconds):"
previous_stats=""
while true; do
  stats=$(curl -s -X GET http://localhost:8083/api/signing/stats)
  if [[ $stats != $previous_stats ]]; then
    echo $stats
  fi
  previous_stats=$stats
  sleep 5
done
