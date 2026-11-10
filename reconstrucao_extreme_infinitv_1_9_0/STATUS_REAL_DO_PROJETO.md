# Status real do projeto — 2026-11-10

## Resumo

A reconstrução Java/Android independente implementa múltiplas fontes Xtream/M3U, biblioteca local cifrada, consultas EPG/VOD/séries, player Media3, exportação M3U, backup cifrado e downloads HTTP diretos. JUnit unitários passaram e testes instrumentados foram compilados; ainda falta executá-los no emulador. A referência foi examinada estaticamente; o APK original não foi executado. Consulte `INVENTARIO_ESTADO_INICIAL.md`, `AUDITORIA_REFERENCIA.md` e as matrizes.

## Build e ambiente

- SDK local `$HOME/android-sdk`: API 35 e Build Tools 35.0.0. Projeto usa JDK 17, Gradle Wrapper 8.7 e AGP 8.6.1.
- `:app:testDebugUnitTest` APROVADO (7 testes); `:app:compileDebugAndroidTestJavaWithJavac` APROVADO (compila, não executa os testes instrumentados).
- `:app:assembleDebug` APROVADO. APK local: `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `ff63b1dec44a029479d224cd301476796477a3a8283e4d1a878bb173cac3b66c`; assinatura Debug v2 validada e package/SDK conferidos via aapt.
- Não há binário Emulator neste Termux; testes instrumentados requerem Actions/emulador.
- Actions no commit `b8c017d` passou JUnit e Debug; Release foi pulado sem secrets de assinatura. Workflow atualizado com testes instrumentados aguarda a próxima execução.
- O workflow Python preexistente falhou nos 42 testes: 3 falhas e 2 erros dentro de `tests/test_ai.py`/banner. Seus arquivos estavam fora do escopo e foram preservados.

## Implementado com evidência no código

- Fontes múltiplas, Xtream (auth, categorias, VOD, episódios, EPG) e M3U URL/arquivo local.
- Favoritos/Watchlist/histórico e credenciais/URLs cifrados pelo Android Keystore; backup portátil AES-GCM com frase secreta.
- Player Media3 HLS/DASH com cabeçalhos, seleção de áudio/legenda, PiP; UX Leanback/focável.
- Editor/exportação M3U e downloads HTTP resumíveis para arquivos diretos com reprodução offline.
- Unit tests executados; testes instrumentados de armazenamento, backup e Xtream mock compilados.

## Ajustes desta continuação

- `XtreamClient` resolve episódio via `get_series_info` sem usar `series_id` como ID de mídia.
- Ao remover favorito enquanto a lista Favoritos está aberta, a linha é removida da exibição imediatamente.
- `git diff --check` não apontou whitespace problemático nos arquivos rastreados consultados; arquivos do projeto são em grande parte não rastreados no estado Git apresentado.

## Não comprovado

Não testados em aparelho/serviço real: reprodução/codec, D-pad/TV, Android 10 e provedores Xtream/M3U. Busca não agrega fontes descarregadas, XMLTV/DRM não estão cobertos e downloads HLS/DASH segmentados não são suportados. Tests instrumentados ainda aguardam emulador. Release assinado depende de secrets privados não configurados.
