# Matriz de funcionalidades

Classificação pela implementação encontrada, sem inferir comportamento a partir de nomes de tela. `NÃO TESTADO` indica que existe código que não foi executado em Android ou contra servidor controlado.

| Área | Estado | Evidência/limite |
|---|---|---|
| Login e gerenciamento de fontes | PARCIAL | Formulário Xtream/M3U; uma única fonte, sem CRUD/seleção múltipla. |
| Múltiplas conexões Xtream | AUSENTE | `LocalStore` guarda apenas uma fonte. |
| Importação M3U/M3U8 | PARCIAL | Download por URL e parser EXTINF/EXTGRP; sem seletor de arquivo, serialização, URL relativa e cabeçalhos. |
| Categorias de TV ao vivo | PARCIAL | Busca categorias Xtream e filtro M3U local; UX/falhas sem validação. |
| Catálogo de filmes | PARCIAL | Lista Xtream/M3U; detalhe não implementado. |
| Séries, temporadas e episódios | PARCIAL | Busca catálogo de séries; botão informa que detalhes não estão disponíveis. |
| Detalhes de filmes e séries | AUSENTE | Não há fluxo de detalhe/temporada. |
| EPG e programação atual/próxima | AUSENTE | Sem parser XMLTV nem consulta/exibição. |
| Favoritos | PARCIAL | Grava JSON em SharedPreferences; não segmentado por fonte nem testado. |
| Biblioteca e histórico | AUSENTE | Sem histórico/progresso/watchlist. |
| Busca global | PARCIAL | Filtro local por nome apenas nos dados carregados. |
| Player e seleção de mídia | PARCIAL | Media3 fullscreen; sem seleção de faixa/headers; playback NÃO TESTADO. |
| Downloads | AUSENTE | Sem fila/armazenamento offline. |
| Edição e gerenciamento de playlists | AUSENTE | Não há editor/exportador nem gestão de fontes. |
| Backup e restauração | AUSENTE | Sem fluxo ou formato de backup. |
| Preferências e persistência | PARCIAL | Preferência privada simples; fonte única e favoritos, senha sem cifra adicional; sem migração. |
| Tratamento de erros de rede | PARCIAL | Exceções viram Toast; códigos HTTP, retry, cancelamento e erros específicos ausentes. |
| Proteção de credenciais | PARCIAL | Não há credencial embutida no código; senha gravada em SharedPreferences sem Keystore e pode compor URL de mídia. |
| Android TV/D-pad | PARCIAL | Launcher Leanback e controles focáveis; fluxo integral/foco e layouts grandes não verificados. |
