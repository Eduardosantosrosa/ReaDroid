# Inventário inicial — 2026-11-10

Este registro foi criado antes de alterações de implementação, conforme a solicitação de preservação. Nenhum arquivo preexistente foi sobrescrito nesta etapa.

## Escopo examinado

O workspace contém diversos projetos e artefatos preexistentes: ferramenta ReaDroid, Android helper, reconstruções de outros aplicativos, `output/`, `backups/`, relatórios e arquivos compilados. Há alterações rastreadas e não rastreadas fora do alvo; elas foram preservadas. O alvo desta continuação é `reconstrucao_extreme_infinitv_1_9_0/`.

No alvo, os arquivos existentes são: `.gitignore`, `LICENSE`, `README.md`, `ANALISE_GERAL.md`, `ARQUITETURA.md`, `COMPARACAO_APK_CODIGO.md`, `FUNCIONALIDADES.md`, `INTEGRACAO_ANDROID.md`, `LIMITACOES_E_PENDENCIAS.md`, `MATRIZ_DE_TESTES.md`, `PERSISTENCIA_E_DADOS.md`, `PLAYER_E_STREAMING.md`, `TELAS_E_NAVEGACAO.md`, arquivos Gradle de raiz/app, manifesto, estilos, seis classes Java de produção e um arquivo JUnit (`M3uParserTest.java`). Não há wrapper Gradle no projeto-alvo.

## Código e estado observado

- `MainActivity`: tela única construída programaticamente, importa URL M3U, coleta credenciais Xtream, alterna seções, consulta categorias/listas, pesquisa nomes, alterna favoritos persistidos e abre player. É comportamento básico; não equivale a fluxos completos.
- `M3uParser`: extrai EXTINF/EXTGRP, título, grupo e logo, e só aceita URLs absolutas HTTP(S); sem serialização, importação local, resolução relativa ou parsing de temporadas/headers.
- `XtreamClient`: valida `player_api.php`, busca categorias e listas live/VOD/séries; gera URLs live/movie. Não implementa detalhes de série/episódio, EPG, paginação, cancelamento, retry, exportação ou validação robusta de status HTTP.
- `LocalStore`: grava uma única fonte em `SharedPreferences` (inclusive senha em texto claro no armazenamento privado) e lista JSON de favoritos. Sem Keystore, schema/migração, backup, biblioteca/histórico ou catálogo persistente.
- `StreamItem`: modelo simples com id, nome, URL, categoria, tipo e logo.
- `VideoActivity`: Media3 fullscreen, URL direta e toast de erro. Sem cabeçalhos, faixas customizadas, foreground service ou tratamento de lifecycle além de pause/release.
- Testes: quatro testes JUnit do parser escritos. Histórico documental menciona quatro cenários via `javac` harness, mas não há evidência de execução JUnit/Gradle nesta inspeção.

## Configuração

Gradle Android plugin 8.6.1, compileSdk 35, targetSdk 35, minSdk 26, Java 17; Media3 1.5.1 e JUnit 4.13.2. Manifesto permite cleartext global e declara launchers normal/Leanback; não há layout TV próprio nem dependência Leanback. Não existe Gradle wrapper.

## Ambiente visto nesta execução

JDK/Javac 21.0.13; Gradle e Node existem no PATH; pnpm, Rust, Cargo, adb e sdkmanager não foram encontrados. `ANDROID_HOME` e `ANDROID_SDK_ROOT` estão vazios. `aapt` existe no PATH, mas isso isoladamente não constitui Android SDK de compilação. Nenhum APK do projeto-alvo existia no inventário.

## Integridade e ressalvas

A referência e APK indicados em `~/storage/downloads/...` foram acessíveis para leitura durante a auditoria posterior; a leitura dos módulos originais está resumida em `AUDITORIA_REFERENCIA.md`. A fonte original e APK original não foram alterados. Classificações funcionais são auditoria estática da reconstrução contra os fluxos observados; não equivalem à execução do original.
