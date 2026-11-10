# Matriz de testes

## Executados nesta continuação

| Caso | Estado | Resultado |
|---|---|---|
| Gradle `:app:testDebugUnitTest` offline | FALHOU NA CONFIGURAÇÃO | Wrapper 8.7 iniciou, mas AGP 8.6.1 não estava no cache offline. |
| Gradle `:app:testDebugUnitTest` com resolução normal | FALHOU NA CONFIGURAÇÃO | AGP foi resolvido; build parou com `SDK location not found`, antes de compilar ou rodar JUnit. |
| Gradle `help` offline | APROVADO | Wrapper e configuração Gradle/AGP do projeto carregaram; tarefa não compila código Android. |
| JUnit M3U (4 casos existentes) | NÃO EXECUTADO | Nenhum caso executado porque falta Android SDK Platform. |
| APK debug / inspeção | NÃO EXECUTADO | Nenhum APK deste projeto foi criado. |
| APK original e app original | NÃO EXECUTADO | Foi feita análise estática da árvore de fonte e dos relatórios de inspeção; o APK não foi instalado nem executado. |
| XML do manifesto e banner TV | APROVADO (ESTÁTICO) | Os dois arquivos foram analisados por `xml.etree.ElementTree`; isso não equivale ao AAPT/build Android. |
| `git diff --check` | APROVADO | Sem erros de whitespace no diff atual. |

## Testes existentes

`app/src/test/.../M3uParserTest.java` contém quatro casos escritos: metadados entre aspas/grupo/vírgula; EXTGRP e CRLF; comentário e URI inválida; entrada vazia. Eles não são testes aprovados até execução pelo JUnit.

## Cobertura requerida

| Área/caso | Estado |
|---|---|
| Parser M3U: EXTINF, aspas, vírgula, CRLF e URI inválida | CRIADO (4 casos), NÃO EXECUTADO |
| Serialização M3U round-trip | NÃO CRIADO |
| Xtream API e auth com servidor simulado | NÃO CRIADO |
| Categorias e filtro | NÃO CRIADO |
| Xtream get_series_info e seleção de episódio | FLUXO IMPLEMENTADO; teste automatizado e teste contra servidor simulado NÃO CRIADOS |
| Detalhes de filme e metadados completos de série | NÃO IMPLEMENTADO |
| XMLTV/EPG, fuso e now/next | NÃO CRIADO (função ausente) |
| Favoritos por fonte e persistência/reinício | NÃO CRIADO |
| Múltiplas fontes/migração | NÃO CRIADO (função ausente) |
| Navegação, D-pad, foco, back e acessibilidade | NÃO CRIADO; teste manual em dispositivo pendente |
| Player HLS, DASH, TS, headers, lifecycle, áudio/legendas | NÃO CRIADO; teste manual pendente |
| Rede: offline, timeout, HTTP status e JSON inválido | NÃO CRIADO |
| Android 10 (API 29) | TESTE MANUAL PENDENTE |
| Android TV 720p/1080p | TESTE MANUAL PENDENTE |

Usar fixtures sintéticas e servidor local simulado; não usar credenciais reais.
