#!/usr/bin/env bash
# Usage:  ./push_to_github.sh <your-github-username> <repo-name>
# Example: ./push_to_github.sh myuser java-assignment-1-2
# 1) Create an EMPTY repo called <repo-name> on github.com first (no README/.gitignore),
#    or let this script create it if the GitHub CLI (gh) is installed and logged in.
set -e
USER_NAME="$1"; REPO="$2"
[ -z "$USER_NAME" ] || [ -z "$REPO" ] && { echo "Usage: $0 <github-username> <repo-name>"; exit 1; }

git init -b main
git add .
git commit -m "Java assignment 1 & 2: bank system, applet, student records manager, essays"

if command -v gh >/dev/null 2>&1; then
  gh repo create "$USER_NAME/$REPO" --public --source=. --remote=origin --push
  # add the collaborator (needs their GitHub USERNAME)
  echo "To add the collaborator by username run:"
  echo "  gh api -X PUT repos/$USER_NAME/$REPO/collaborators/<their-github-username>"
else
  git remote add origin "https://github.com/$USER_NAME/$REPO.git"
  git push -u origin main
fi
echo "Now add stanslausphillimon@gmail.com: GitHub repo > Settings > Collaborators > Add people."
