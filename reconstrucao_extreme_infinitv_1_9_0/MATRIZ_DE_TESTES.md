# Matriz de testes

## Executados nesta continuação

| Caso | Estado | Resultado |
|---|---|---|
| Gradle `testDebugUnitTest` offline | NÃO EXECUTADO | Duas tentativas ficaram sem progresso observável durante a inicialização do daemon; a segunda encerrou pelo limite de 30 s (timeout). Nenhum resultado de teste. |
| JUnit M3U (4 casos existentes) | NÃO EXECUTADO | AGP requerido não disponível no cache; SDK Android não localizado. |
| APK debug / inspeção | NÃO EXECUTADO | Nenhum APK deste projeto foi criado. |
| APK original e app original | NÃO EXECUTADO | Foi feita análise estática da árvore de fonte e dos relatórios de inspeção; o APK não foi instalado nem executado. |

## Testes existentes

`app/src/test/.../M3uParserTest.java` contém quatro casos escritos: metadados entre aspas/grupo/vírgula; EXTGRP e CRLF; comentário e URI inválida; entrada vazia. Eles não são testes aprovados até execução pelo JUnit.

## Cobertura requerida

| Área/caso | Estado |
|---|---|
| Parser M3U: EXTINF, aspas, vírgula, CRLF e URI inválida | CRIADO (4 casos), NÃO EXECUTADO |
| Serialização M3U round-trip | NÃO CRIADO |
| Xtream API e auth com servidor simulado | NÃO CRIADO |
| Categorias e filtro | NÃO CRIADO |
| Detalhes de filme/série/temporada/episódio | NÃO CRIADO (fluxo ausente) |
| XMLTV/EPG, fuso e now/next | NÃO CRIADO (função ausente) |
| Favoritos por fonte e persistência/reinício | NÃO CRIADO |
| Múltiplas fontes/migração | NÃO CRIADO (função ausente) |
| Navegação, D-pad, foco, back e acessibilidade | NÃO CRIADO; teste manual em dispositivo pendente |
| Player HLS, DASH, TS, headers, lifecycle, áudio/legendas | NÃO CRIADO; teste manual pendente |
| Rede: offline, timeout, HTTP status e JSON inválido | NÃO CRIADO |
| Android 10 (API 29) | TESTE MANUAL PENDENTE |
| Android TV 720p/1080p | TESTE MANUAL PENDENTE |

Usar fixtures sintéticas e servidor local simulado; não usar credenciais reais.
