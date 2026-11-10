# Status real do projeto — 2026-11-10

## Resumo

A reconstrução Java/Android independente implementa conexão Xtream e M3U remota, catálogo básico, pesquisa/filtro, favoritos locais e player Media3. Nesta etapa, séries Xtream consultam `get_series_info`, listam episódios por temporada e permitem iniciar sua reprodução. Não há APK gerado. A referência foi examinada estaticamente; o APK original não foi executado. Consulte `INVENTARIO_ESTADO_INICIAL.md` e `AUDITORIA_REFERENCIA.md`.

## Build e ambiente

- Java/Javac: OpenJDK 21.0.13 no PATH; Gradle instalado (versão 9.9 na mensagem do daemon).
- Node está no PATH; pnpm, rustc, cargo, adb e sdkmanager ausentes.
- `ANDROID_HOME`/`ANDROID_SDK_ROOT` vazios; nenhuma instalação de `android.jar` encontrada.
- O projeto contém Gradle Wrapper 8.7 e usa AGP 8.6.1, SDK 35 e Java 17.
- `./gradlew --offline --no-daemon --console=plain :app:testDebugUnitTest` falhou antes de configurar tarefas porque AGP 8.6.1 não está no cache offline.
- A tentativa com resolução normal conseguiu configurar o plugin, mas falhou com `SDK location not found`. Não houve compilação Java nem execução JUnit.
- `./gradlew --offline --no-daemon --console=plain help` APROVADO; valida apenas o wrapper/configuração Gradle, não compila o app.
- Nenhum APK foi gerado. Não há caminho de APK deste projeto a reportar.

## Implementado com evidência no código

- Fonte única Xtream: coleta host/usuário/senha, tenta validação e carrega categorias e itens de live/VOD/séries.
- Fonte única M3U/M3U8 por URL HTTP(S): baixa a lista e analisa entradas EXTINF e EXTGRP.
- TV ao vivo, filmes e séries têm navegação básica; listas e alguns filtros dependem de dados do provedor.
- Pesquisa local pelo nome nas entradas carregadas.
- Favoritos serializados em SharedPreferences.
- Player Media3 com módulos HLS/DASH e URL direta; reprodução não foi testada.
- Série Xtream: request `get_series_info`, resolução dos IDs/extensões retornados e seleção do episódio para abrir Media3; não testado contra servidor.
- Workflow GitHub Actions gera Debug e, com secrets de assinatura, Release assinado.

## Ajustes desta continuação

- `XtreamClient` resolve episódio via `get_series_info` sem usar `series_id` como ID de mídia.
- Ao remover favorito enquanto a lista Favoritos está aberta, a linha é removida da exibição imediatamente.
- `git diff --check` não apontou whitespace problemático nos arquivos rastreados consultados; arquivos do projeto são em grande parte não rastreados no estado Git apresentado.

## Não comprovado

EPG/XMLTV, detalhes completos de filmes/séries, múltiplas fontes, credenciais cifradas, histórico/watchlist, downloads, editor/exportação M3U, backup/restauração e testes de integração ainda não estão implementados. Android 10, TV Box, D-pad/foco, acessibilidade, APIs reais e codecs não foram testados. A senha persiste em SharedPreferences sem cifra adicional. Quando o ambiente estiver pronto, os caminhos esperados são `app/build/outputs/apk/debug/app-debug.apk` e, com chave configurada, `app/build/outputs/apk/release/app-release.apk`.
