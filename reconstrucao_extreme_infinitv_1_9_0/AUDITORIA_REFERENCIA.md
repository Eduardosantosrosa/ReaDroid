# Auditoria da referência — leitura estática

Referência examinada em `/data/data/com.termux/files/home/storage/downloads/Extreme-InfiniTV-1.9.0/Extreme-InfiniTV-1.9.0/` e APK em `/data/data/com.termux/files/home/storage/downloads/Extreme-InfiniTV.apk`. Leitura sem modificação dos originais e sem execução/instalação do APK.

## Evidência de módulos no código-fonte

A árvore `src/scripts/lib` contém módulos separados `xtream-api.js`, `m3u-parser.ts`, `m3u-serializer.ts`, `series-seasons.ts`, `epg-worker.ts`, `short-epg.ts`, `backup-snapshot.ts`, `restore-backup.ts`, `downloads.js`, `player-runtime.js` e `custom-playlist.ts`. Busca no código encontrou `get_series_info`, parsing XMLTV em worker dedicado (com fluxo now-next e rejeição de DOCTYPE/ENTITY), serialização M3U, cache de EPG, backup/restauração e serviços de downloads. Rotas e componentes já documentados indicam páginas e shells TV.

A presença desses módulos prova que há implementação de software nesses domínios, mas não atesta sucesso contra qualquer servidor, codec ou dispositivo. O APK também foi analisado estaticamente nos relatórios existentes; não houve validação dinâmica.

## Comparação com o projeto independente

O independente possui parser básico, cliente API Xtream direto, armazenamento simples, listagem e playback. Não possui os módulos/casos de uso acima para série/episódio, XMLTV, exportação/edição M3U, backup, downloads ou shell TV dedicado. A correspondência de nome/classe não foi tratada como paridade.

Foi corrigida uma inconsistência na reconstrução durante esta continuação: `series_id` não é convertido em URL `/movie/...`; o cliente deixa URL vazia até existir resolução de episódio via `get_series_info`. Isso não implementa o fluxo de séries.

## GPL

O fonte observado declara GPL-3.0-or-later. Nenhum código foi copiado ou adaptado para a reconstrução; esta auditoria registra caminhos e nomes de módulos como evidência. Se uma continuação reutilizar código GPL, deverá preservar avisos/licença e cumprir obrigações de distribuição do código-fonte correspondente.

## Inventário funcional estático

As rotas `.astro` em `src/pages/` comprovam telas desktop para início, login, TV ao vivo, filmes e detalhes, séries e detalhes, busca, EPG, favoritos, watchlist, adicionados recentemente, downloads, editor M3U, receiver e configurações. O subdiretório `src/pages/tv/` traz rotas próprias de home, login, live, movies, series, busca e configurações; `src/layouts/TvLayout.astro` e `src/layouts/ReceiverLayout.astro` confirmam shells distintos. Isso prova rotas e componentes existentes, não que todos os controles foram exercitados.

| Área | Evidência examinada | O que o código indica |
|---|---|---|
| Login/fontes | `src/pages/login.astro`, `src/scripts/lib/creds.js`, `xtream-api.js`, `m3u-parser.ts` | Fluxos Xtream, M3U por URL e playlist local; Xtream admite mirrors/failover. Sem serviço de autenticação próprio: credenciais são do provedor inseridas pelo usuário. |
| Catálogo/API | `src/scripts/lib/xtream-api.js`, `catalog.js`, páginas live/movies/series | Consultas de categorias e conteúdo; detalhes de série chamam API de episódios. Comportamento depende do servidor Xtream. |
| M3U | `m3u-parser.ts`, `m3u-serializer.ts`, `custom-playlist.ts`, `src/scripts/editor/editor.ts` | Parsing, serialização e edição de playlists; inclui suporte a playlists personalizadas e metadados de stream. |
| Reprodução | `player-runtime.js`, `android-video-launcher.ts`, `src-tauri/src/external_player.rs`, `vod_proxy.rs`, `vod_audio_proxy.rs` | Runtime e handoff nativo/externo, proxies VOD/áudio e lógica Android; não valida codecs nem sucesso de streams reais. |
| EPG | `src/scripts/lib/epg-worker.ts`, `short-epg.ts`, `src/scripts/epg/epg.ts`, `mapping.ts` | Parser XMLTV em worker, cache/visão now-next e mapeamento de canais conforme fluxo encontrado. |
| Favoritos/biblioteca | páginas favorites/watchlist/recently-added e componentes `AllFavoritesView`, `AllWatchlistView`, `ContinueWatching` | Favoritos, itens para assistir depois, continuidade e recentes. Persistência é através dos stores em `src/scripts/lib`; execução dinâmica não foi feita. |
| Downloads | `src/scripts/lib/downloads.js`, `src/pages/downloads.astro` | Fila e estados de download, pausa/retomada/remoção e tela de acompanhamento. |
| Backup/configuração | `backup-snapshot.ts`, `restore-backup.ts`, `app-settings.js`, `src/pages/settings.astro` | Snapshot/restauração e preferências; não foi feita prova de round-trip em ambiente. |
| Controles e dispositivos | componentes `src/components/*`, `src/layouts/TvLayout.astro`, páginas TV, manifestos e assets em `src-tauri/gen/android` | Há componentes/layouts TV e integração Android; não foi realizada sessão com toque, D-pad, leitor de tela, TV física ou emulador. |

## APK e limites da comparação

O APK fornecido tem 46.593.936 bytes. A comparação de metadados registra package `com.infinitel8p.xtream`, versão `1.9.0`, versionCode `109009000`, minSdk 26, targetSdk 36, `libapp_lib.so` para quatro ABIs e launcher Leanback. O projeto-fonte declara Tauri e uma `VideoActivity`, o que é coerente estruturalmente. Não houve decompilação DEX ou execução do APK; logo, telas, lógica e correspondência de cada controle no binário não foram validadas. Ver `COMPARACAO_APK_CODIGO.md` para hash/assinatura e divergências de manifesto.
