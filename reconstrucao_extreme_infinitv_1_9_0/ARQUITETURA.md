# Arquitetura

## Original observado

- `src/pages`: páginas Astro para login, home, TV ao vivo, filmes e séries, EPG, busca, favoritos, watchlist, downloads, edição de playlist, configurações e receiver; há uma variante de rotas e layouts para TV.
- `src/components`: shells e componentes Astro/Svelte de lista, home, TV, player e dialogs.
- `src/scripts/lib`: módulos de serviço e domínio (catálogo, credenciais, M3U, EPG, catch-up, player, preferências, persistência, rede, downloads e receiver).
- `src-tauri/src`: comandos e serviços Rust, incluindo proxy de mídia, áudio, HTTP range, player externo e receiver.
- `src-tauri/gen/android`: wrapper Android Kotlin/Gradle, manifesto, layouts para player e Activity, recursos TV e configuração de rede.

Fluxo de build: Astro/Vite compila `frontendDist` (`dist`); `tauri android build` empacota frontend no shell Android e compila a biblioteca Rust nativa. Configuração do produto e bundle em `src-tauri/tauri.conf.json`.

## Reconstrução

Camadas: `MainActivity` apresenta catálogo/importação; `M3uParser` analisa EXTINF e URL; `XtreamClient` consulta API do host configurado; `LocalStore` guarda fonte/favoritos em SharedPreferences; `VideoActivity` delega decodificação a Media3. Rede acontece numa fila de trabalho e UI volta à thread principal. A API de playback recebe uma URL, sem relay intermediário.

## Limites atuais

A Activity é única e desenha views nativas programaticamente. Não há arquitetura offline, repositório paginado, DI, banco relacional, gestão de cancelamento de requisição ou adaptador de séries. Listas grandes permanecem inteiras em memória. Projeto independente não inclui a complexidade dos serviços nativos observados no original.
