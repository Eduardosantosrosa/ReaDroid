# Análise geral

## Escopo e fontes

Análise estática do diretório `Extreme-InfiniTV-1.9.0/Extreme-InfiniTV-1.9.0`, do ZIP correspondente e do APK `Extreme-InfiniTV.apk`. Não houve instalação nem execução do APK. A inspeção foi de manifest, recursos compactados, assinatura e arquivos de build. O fonte contém 1.9.0, licença GPL-3.0-or-later; a pasta fonte foi recebida sem metadados Git acessíveis nesta inspeção.

## Arquitetura e tecnologia

Aplicação Tauri 2: interface Astro 7 + Svelte 5 + TypeScript/JavaScript, compilada para frontend web; camada nativa Rust/Tauri. O wrapper Android gerado usa Kotlin/Gradle, WebView Tauri e código Android nativo para reprodução, receiver e serviços. Dependências visíveis incluem Video.js, Artplayer, hls.js, mpegts.js, Shaka Player e Media3 no wrapper Android. O código Rust inclui HTTP/proxy, player externo, receiver, firewall, warmup, caminho de armazenamento e proxy de áudio/vídeo.

O `package.json` declara pnpm 10, scripts build/test Tauri e Vitest. Cargo declara Rust 2021, rust-version 1.77.2, Tauri 2.11 e vários plugins. O app Android gerado usa minSdk 26/targetSdk 36, Media3 1.10.1 e Java/Kotlin 17.

## Funcionalidades identificadas

Foi possível identificar **36 áreas funcionais** no código, resumidas em `FUNCIONALIDADES.md`. Evidência é a implementação existente em `src/pages`, `src/scripts/lib`, `src-tauri/src` e `src-tauri/gen/android`; isso não representa validação em execução. O repo tem 150 arquivos de teste unitário/integração/e2e, mas não foram executados nesta fase.

## APK

`aapt dump badging` confirma `com.infinitel8p.xtream`, `1.9.0`, versionCode 109009000, compile/target SDK 36 e minSdk 26. `apksigner verify --print-certs` validou assinatura V2; certificado DN `CN=InfiniteL8p` com SHA-256 `1d3ab36fe1380db82e30ec1d86b43f0690eb6139383cd4ee4a5cdc8608ed46e2`. O pacote inclui `libapp_lib.so` para arm64-v8a, armeabi-v7a, x86 e x86_64 e DEX, compatíveis com o app Tauri/Rust declarado. Manifesto registra MainActivity, VideoActivity, XtreamDreamService, ReceiverForegroundService e FileProvider, além de receivers/providers de bibliotecas Android. O ZIP do APK contém 843 entradas em `res/`, 4 bibliotecas nativas (uma por ABI), 1 DEX e 3 arquivos em `assets/` (config Tauri e perfis baseline). Há uma fonte TTF em `res/`.

## Reconstrução criada

O projeto separado `reconstrucao_extreme_infinitv_1_9_0/` é uma implementação Android Java independente, package `org.readroid.extremerebuild`, minSdk 26. Implementa importação remota M3U, consultas básicas Xtream, listagem e categorias de TV/VOD, pesquisa, favoritos e playback Media3. Não foi derivado de arquivos GPL: nenhum código, imagem, fonte, ícone ou marca do original foi copiado.

## Integridade

Os originais não foram editados. O APK foi apenas lido por ferramentas estáticas. Não há credenciais ou playlists incluídas na reconstrução. A interface comunica diretamente com o host IPTV fornecido pelo usuário. O nome de produto original não é usado como nome ou identidade visual do projeto novo.
