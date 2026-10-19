#!/bin/bash
set -e

cd /home/ubuntu/ClojureDocs

git fetch origin master -q
LOCAL=$(git rev-parse HEAD)
REMOTE=$(git rev-parse origin/master)

if [ "$LOCAL" = "$REMOTE" ]; then
  exit 0
fi

echo "$(date -Iseconds) deploying $LOCAL -> $REMOTE"
git pull --ff-only
sudo -u ubuntu /usr/local/bin/lein deps
sudo -u ubuntu /usr/local/bin/lein compile
sudo -u ubuntu /usr/local/bin/lein cljsbuild once prod
sudo systemctl restart clojuredocs
echo "$(date -Iseconds) deploy complete, now at $(git rev-parse HEAD)"
echo "Check status with: sudo systemctl status clojuredocs"