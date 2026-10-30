#!/data/data/com.termux/files/usr/bin/bash
set -e
cd "$HOME/controle_conso"
git init -b main
git add .
git -c user.name="janintibo-art" -c user.email="janintibo-art@users.noreply.github.com" commit -m "Controle Conso V1 : Android, site, logo et GitHub Actions"
gh repo create janintibo-art/controle-conso --public --source . --remote origin --push
printf '\nDepot cree : https://github.com/janintibo-art/controle-conso\n'
