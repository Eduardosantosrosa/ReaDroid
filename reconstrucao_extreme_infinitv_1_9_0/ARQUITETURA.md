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

Camadas atuais: `MainActivity` oferece fontes, catálogo, busca, biblioteca, EPG, detalhes, edição/compartilhamento M3U, configurações e fila de downloads; `M3uParser`/`M3uSerializer` analisam e exportam entradas e cabeçalhos suportados; `XtreamClient` consulta o host configurado, metadados VOD, EPG curto e episódios; `LocalStore` mantém múltiplas fontes, histórico/favoritos/watchlist/downloads e preferências, cifrando dados sensíveis com AES-GCM e chave Android Keystore; `BackupManager` cria snapshots portáteis cifrados por frase secreta; `MediaDownloadService` baixa arquivos HTTP diretos com retomada; `VideoActivity` usa Media3 (HLS/DASH) e oferece seleção de faixas e PiP. Operações de rede rodam fora da thread de UI. TV usa launcher Leanback, widgets focáveis e realce de foco.

## Limites atuais

Limites: Activity única e views programáticas; catálogo não federado/paginado e busca somente sobre conteúdo carregado; sem XMLTV, DRM nem download de HLS/DASH segmentado. Dados da biblioteca são locais ao app; migração/restauração de downloads não copia arquivos de mídia. Testes cobrem parser, store/backup e Xtream mock, mas não exercitam UI, transporte de download ou codecs. A reconstrução não reproduz os serviços/proxies do original e não afirma equivalência total.
