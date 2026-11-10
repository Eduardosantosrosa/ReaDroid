# Arquitetura

## Original observado

- `src/pages`: páginas Astro para login, home, TV ao vivo, filmes e séries, EPG, busca, favoritos, watchlist, downloads, edição de playlist, configurações e receiver; há uma variante de rotas e layouts para TV.
- `src/components`: shells e componentes Astro/Svelte de lista, home, TV, player e dialogs.
- `src/scripts/lib`: módulos de serviço e domínio (catálogo, credenciais, M3U, EPG, catch-up, player, preferências, persistência, rede, downloads e receiver).
- `src-tauri/src`: comandos e serviços Rust, incluindo proxy de mídia, áudio, HTTP range, player externo e receiver.
- `src-tauri/gen/android`: wrapper Android Kotlin/Gradle, manifesto, layouts para player e Activity, recursos TV e configuração de rede.

Fluxo de build: Astro/Vite compila `frontendDist` (`dist`); `tauri android build` empacota frontend no shell Android e compila a biblioteca Rust nativa. Configuração do produto e bundle em `src-tauri/tauri.conf.json`.

## Reconstrução

**Tecnologia escolhida: Java nativo para Android.** O alvo é Android/Android TV, e a implementação precisa de Media3, intents, ciclo de vida, armazenamento Android e foco/D-pad sem camada de runtime adicional. Kotlin também seria apropriado, mas a reconstrução existente e o código auditado estão em Java; manter essa base reduz migração e facilita Gradle/Android Studio. Python serve para ferramentas de auditoria e testes auxiliares, mas exigiria um runtime ou bridge e não oferece vantagem no player/TV.

Camadas: `MainActivity` apresenta catálogo/importação; `M3uParser` analisa EXTINF e URL; `XtreamClient` consulta API do host configurado e resolve episódios via `get_series_info`; `LocalStore` guarda fonte/favoritos em SharedPreferences; `VideoActivity` delega decodificação a Media3. Rede acontece numa fila de trabalho e UI volta à thread principal. A API de playback recebe uma URL, sem relay intermediário.

## Limites atuais

A Activity é única e desenha views nativas programaticamente. Não há arquitetura offline, repositório paginado, DI, banco relacional, gestão de cancelamento de requisição ou adaptador de séries. Listas grandes permanecem inteiras em memória. Projeto independente não inclui a complexidade dos serviços nativos observados no original.
