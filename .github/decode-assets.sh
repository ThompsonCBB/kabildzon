#!/usr/bin/env bash
# Rebuilds binary files uploaded as base64 text parts: <file>.b64.part00, part01, ...
set -euo pipefail
find . -name '*.b64.part*' -not -path './.git/*' | sed -E 's/\.b64\.part[0-9]+$//' | sort -u | while read -r target; do
  cat $(ls "$target".b64.part* | sort) | base64 -d > "$target"
  if [ "${1:-}" = "--git" ]; then git rm -q "$target".b64.part*; else rm -f "$target".b64.part*; fi
  echo "decoded $target"
done
