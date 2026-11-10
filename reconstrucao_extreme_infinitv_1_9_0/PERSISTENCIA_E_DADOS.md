# Persistência e dados

## Original

O frontend usa `localStorage` para preferências, lista de playlists e dados de UI; IndexedDB aparece em módulos como cache (`xt_cache`) e conteúdo local (`xt_local_content`). Tauri Store plugin e Android FS plugin são declarados e escopos de arquivos Android cobrem `$APPDATA`/logs. Há bibliotecas para backup/restore, downloads, progresso local e credenciais. Os caches incluem dados remotos e timestamps. Não foi encontrado banco SQLite na configuração inspecionada. Credenciais de fonte são necessárias para API Xtream, mas os caminhos exatos de segredo precisam ser lidos por recurso antes de afirmar criptografia em repouso.

Entradas/saídas típicas: URL M3U produz entradas de stream; API Xtream fornece categorias/streams/account info; EPG XML entra em worker/cache e gera programas; favoritos, ordenação, watchlist e preferências alteram armazenamento local; backup serializa snapshot de preferências/biblioteca.

## Reconstrução

`SharedPreferences` privado do app armazena tipo da fonte, host, usuário, senha, URL e JSON de favoritos. A senha fica em armazenamento privado Android, mas sem cifragem adicional via Keystore; backups de sistema não foram configurados. Lista M3U permanece em memória e é baixada novamente na abertura. Dados podem ser removidos ao desinstalar. Falta exportação/importação, schema versionado, migração, controle de concorrência e persistência de catálogo/cache.

Nunca embutir credenciais reais em builds, logs ou screenshots. O usuário deve validar se o host é confiável; credenciais são enviadas ao host configurado via HTTP(S) pelo protocolo Xtream.
