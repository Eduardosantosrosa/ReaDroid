# Matriz de testes

## Execução local — 2026-11-10

| Caso | Estado | Resultado |
|---|---|---|
| `:app:testDebugUnitTest` (offline, SDK 35) | APROVADO | 7 testes JUnit passaram (parser, cabeçalhos e serialização M3U). |
| `:app:compileDebugAndroidTestJavaWithJavac` | APROVADO | Classes de teste instrumentado compiladas; execução no emulador pendente. |
| `:app:assembleDebug` | APROVADO | APK gerado e assinado Debug; apksigner verificou v2, aapt identificou package e min/target SDK. |
| Testes instrumentados no emulador | NÃO EXECUTADO LOCALMENTE | O SDK Termux não contém Android Emulator; workflow remoto atualizado deverá executar. |
| APK Debug do Actions e Release | AINDA NÃO VERIFICADO | A execução Actions para os arquivos mais recentes ainda será iniciada pelo push; secrets Release não foram encontrados/configurados. |
| Workflow Python do repositório | FALHOU (preexistente) | Execução anterior: 42 testes, 3 failures e 2 errors em `tests/test_ai.py`/banner, fora desta reconstrução. |
| APK original e app original | NÃO EXECUTADO | Houve auditoria estática; referência não instalada. |
| APK original e app original | NÃO EXECUTADO | Foi feita análise estática da árvore de fonte e dos relatórios de inspeção; o APK não foi instalado nem executado. |
| XML do manifesto e banner TV | APROVADO (ESTÁTICO) | Os dois arquivos foram analisados por `xml.etree.ElementTree`; isso não equivale ao AAPT/build Android. |
| `git diff --check` | APROVADO | Sem erros de whitespace no diff atual. |

## Testes existentes

`app/src/test/.../M3uParserTest.java` contém sete casos JUnit executados com sucesso. Testes Android instrumentados estão em `app/src/androidTest/...`: fontes e criptografia/migração, backup errado/correto, fila de downloads e Xtream com HTTP local simulado. Foram compilados, mas ainda não rodaram no emulador.

## Cobertura requerida

| Área/caso | Estado |
|---|---|
| Parser M3U: EXTINF, aspas, vírgula, CRLF e URI inválida | CRIADO (4 casos), NÃO EXECUTADO |
| Serialização M3U round-trip | APROVADO (JUnit) |
| Xtream get_series_info e EPG com servidor simulado | CRIADO e compilado; aguardando emulador |
| Categorias e filtro | NÃO CRIADO |
| Xtream get_series_info e seleção de episódio | FLUXO IMPLEMENTADO; teste automatizado e teste contra servidor simulado NÃO CRIADOS |
| Detalhes de filme e metadados completos de série | NÃO IMPLEMENTADO |
| XMLTV/EPG, fuso e now/next | NÃO CRIADO (função ausente) |
| Favoritos, fontes, migração, segredo em repouso e backup | CRIADO e compilado; aguardando emulador |
| Downloads: URL cifrada | CRIADO e compilado; aguardando emulador; transporte ainda sem teste instrumentado |
| Navegação, D-pad, foco, back e acessibilidade | NÃO CRIADO; teste manual em dispositivo pendente |
| Player HLS, DASH, TS, headers, lifecycle, áudio/legendas | NÃO CRIADO; teste manual pendente |
| Rede: offline, timeout, HTTP status e JSON inválido | NÃO CRIADO |
| Android 10 (API 29) | TESTE MANUAL PENDENTE |
| Android TV 720p/1080p | TESTE MANUAL PENDENTE |

Usar fixtures sintéticas e servidor local simulado; não usar credenciais reais.
