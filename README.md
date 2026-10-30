# Contrôle Conso — V1

Application Android native (Java) pour consulter les statistiques des **données mobiles** par application : premier plan et arrière-plan, sur 24 h, 7 jours et 30 jours. Tri décroissant, recherche, accès à la page Android permettant de gérer les restrictions d'arrière-plan.

## Installation et permissions
- Android 8+ (minSdk 26). Après lancement, appuyer sur « Autoriser l'accès aux statistiques » puis autoriser Contrôle Conso, revenir dans l'application.
- `PACKAGE_USAGE_STATS` est un accès spécial accordé par l'utilisateur dans les réglages.
- `QUERY_ALL_PACKAGES` sert à associer les UID à leurs noms d'applications, pour l'APK distribuée sur GitHub (permission encadrée pour Google Play).
- Sur certains Android, les statistiques sont partielles, différées ou regroupées par UID; les chiffres peuvent différer des relevés opérateur. Aucune mesure temps réel.
- Certains fabricants n'ouvrent pas l'écran précis de restriction; repli sur la fiche de l'application.
- L'application ne peut pas activer/désactiver elle-même la restriction sans privilèges système. Elle ouvre les réglages Android pour l'utilisateur.

## Déploiement
- Actions → Android APK : produit un APK de débogage installable, artéfact téléchargeable + Release `latest` pour le bouton sur le site.
- Actions → Site GitHub Pages : déploie `docs/` (activer **Settings → Pages → Source: GitHub Actions**).
- URL site : `https://janintibo-art.github.io/controle-conso/` après activation.
- La signature debug est adaptée aux tests, pas à une distribution stable : prévoir un keystore de publication conservé pour les prochaines versions.
- Build non vérifié dans cet environnement : vérifier les actions GitHub au premier push.
