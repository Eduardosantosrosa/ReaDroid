# Auditoria da referência — leitura estática

Referência examinada em `/data/data/com.termux/files/home/storage/downloads/Extreme-InfiniTV-1.9.0/Extreme-InfiniTV-1.9.0/` e APK em `/data/data/com.termux/files/home/storage/downloads/Extreme-InfiniTV.apk`. Leitura sem modificação dos originais e sem execução/instalação do APK.

## Evidência de módulos no código-fonte

A árvore `src/scripts/lib` contém módulos separados `xtream-api.js`, `m3u-parser.ts`, `m3u-serializer.ts`, `series-seasons.ts`, `epg-worker.ts`, `short-epg.ts`, `backup-snapshot.ts`, `restore-backup.ts`, `downloads.js`, `player-runtime.js` e `custom-playlist.ts`. Busca no código encontrou `get_series_info`, parsing XMLTV em worker dedicado (com fluxo now-next e rejeição de DOCTYPE/ENTITY), serialização M3U, cache de EPG, backup/restauração e serviços de downloads. Rotas e componentes já documentados indicam páginas e shells TV.

A presença desses módulos prova que há implementação de software nesses domínios, mas não atesta sucesso contra qualquer servidor, codec ou dispositivo. O APK também foi analisado estaticamente nos relatórios existentes; não houve validação dinâmica.

## Comparação com o projeto independente

O independente possui parser básico, cliente API Xtream direto, armazenamento simples, listagem e playback. Não possui os módulos/casos de uso acima para série/episódio, XMLTV, exportação/edição M3U, backup, downloads ou shell TV dedicado. A correspondência de nome/classe não foi tratada como paridade.

Foi corrigida uma inconsistência na reconstrução durante esta continuação: `series_id` não é convertido em URL `/movie/...`; o cliente deixa URL vazia até existir resolução de episódio via `get_series_info`. Isso não implementa o fluxo de séries.

## GPL

O fonte observado declara GPL-3.0-or-later. Nenhum código foi copiado ou adaptado para a reconstrução; esta auditoria registra caminhos e nomes de módulos como evidência. Se uma continuação reutilizar código GPL, deverá preservar avisos/licença e cumprir obrigações de distribuição do código-fonte correspondente.
