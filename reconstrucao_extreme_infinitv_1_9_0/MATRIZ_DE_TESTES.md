# Matriz de testes

## Execução local — 2026-11-10

| Caso | Estado | Resultado |
|---|---|---|
| `:app:testDebugUnitTest` (offline, SDK 35) | APROVADO | 7 testes JUnit passaram (parser, cabeçalhos e serialização M3U). |
| `:app:compileDebugAndroidTestJavaWithJavac` | APROVADO | Classes de teste instrumentado compiladas; execução no emulador pendente. |
| `:app:assembleDebug` | APROVADO | APK gerado e assinado Debug; apksigner verificou v2, aapt identificou package e min/target SDK. |
| Testes instrumentados no emulador API 35 | APROVADO | Workflow `42891896745`, inclui storage/backup e Xtream via HTTP mock local. |
| APK Debug do Actions | APROVADO | Baixado, assinatura v2 verificada; SHA-256 `7ecf7cf89280b0507051183922e9258f8539bf2aa3b5ef353f40e1ba8950f42b`. |
| Release assinado do Actions | NÃO GERADO | Secrets de assinatura não configurados; workflow pulou esta etapa. |
| Workflow Python do repositório | FALHOU (preexistente) | Execução anterior: 42 testes, 3 failures e 2 errors em `tests/test_ai.py`/banner, fora desta reconstrução. |
| APK original e app original | NÃO EXECUTADO | Houve auditoria estática; referência não instalada. |
| APK original e app original | NÃO EXECUTADO | Foi feita análise estática da árvore de fonte e dos relatórios de inspeção; o APK não foi instalado nem executado. |
| XML do manifesto e banner TV | APROVADO (ESTÁTICO) | Os dois arquivos foram analisados por `xml.etree.ElementTree`; isso não equivale ao AAPT/build Android. |
| `git diff --check` | APROVADO | Sem erros de whitespace no diff atual. |

## Testes existentes

`app/src/test/.../M3uParserTest.java` contém sete casos JUnit executados com sucesso. Testes Android instrumentados em `app/src/androidTest/...` passaram no emulador API 35: fontes e criptografia/migração, backup com senha errada/correta, fila de downloads (URL cifrada) e Xtream com HTTP local simulado. A transferência de mídia e a UI ainda não têm teste automatizado.

## Cobertura requerida

| Área/caso | Estado |
|---|---|
| Parser M3U: EXTINF, aspas, vírgula, CRLF e URI inválida | CRIADO (4 casos), NÃO EXECUTADO |
| Serialização M3U round-trip | APROVADO (JUnit) |
| Xtream get_series_info e EPG com servidor simulado | APROVADO no emulador |
| Categorias e filtro | NÃO CRIADO |
| Xtream get_series_info e seleção de episódio | FLUXO IMPLEMENTADO; teste automatizado e teste contra servidor simulado NÃO CRIADOS |
| Detalhes de filme e metadados completos de série | NÃO IMPLEMENTADO |
| XMLTV/EPG, fuso e now/next | NÃO CRIADO (função ausente) |
| Favoritos, fontes, migração, segredo em repouso e backup | APROVADO no emulador |
| Downloads: URL cifrada | APROVADO no emulador; transporte ainda sem teste instrumentado |
| Navegação de seção e foco visual | Teste instrumentado criado/compilado; aguarda Actions |
| Fluxo D-pad/back e acessibilidade em dispositivo físico | TESTE MANUAL PENDENTE |
| Player HLS, DASH, TS, headers, lifecycle, áudio/legendas | NÃO CRIADO; teste manual pendente |
| Rede: offline, timeout, HTTP status e JSON inválido | NÃO CRIADO |
| Android 10 (API 29) | TESTE MANUAL PENDENTE |
| Android TV 720p/1080p | TESTE MANUAL PENDENTE |

Usar fixtures sintéticas e servidor local simulado; não usar credenciais reais.
