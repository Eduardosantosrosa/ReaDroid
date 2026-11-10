# Matriz de funcionalidades

Classificação pela implementação encontrada, sem inferir comportamento a partir de nomes de tela. `NÃO TESTADO` indica que existe código que não foi executado em Android ou contra servidor controlado.

| Área | Estado | Evidência/limite |
|---|---|---|
| Login e gerenciamento de fontes | IMPLEMENTADO NO CÓDIGO | CRUD e seleção Xtream/M3U; sem teste manual contra provedor real. |
| Múltiplas conexões Xtream/M3U | IMPLEMENTADO NO CÓDIGO | Fonte ativa e migração do formato antigo; teste instrumentado escrito. |
| Importação M3U/M3U8 | PARCIAL | URL HTTP/HTTPS e SAF local; parser de entradas e cabeçalhos conhecidos. URLs relativas e playlist master não suportadas. |
| Categorias | IMPLEMENTADO NO CÓDIGO | Categorias Xtream e grupo M3U, filtrados localmente; UX/dispositivo não validado. |
| Catálogo e detalhes de filmes | PARCIAL | Lista e detalhe Xtream por `get_vod_info`; M3U tem metadados limitados. |
| Séries, temporadas e episódios Xtream | IMPLEMENTADO NO CÓDIGO | `get_series_info`, temporadas ordenadas e URI de episódios; teste instrumentado mockado compilado, execução pendente no emulador. |
| EPG | PARCIAL | Consulta Xtream `get_short_epg` e exibição; sem XMLTV e sem teste executado contra provedor. |
| Favoritos, Assistir depois e histórico | IMPLEMENTADO NO CÓDIGO | Persistência cifrada com identificação por fonte; testes instrumentados escritos. |
| Busca global | PARCIAL | Pesquisa de nomes no catálogo atualmente carregado; não agrega fontes em segundo plano. |
| Player e seleção de mídia | PARCIAL | Media3 com HLS/DASH, headers, seleção de faixas e PiP; reprodução em dispositivo não testada. DRM/codec dependem do aparelho/provedor. |
| Downloads | PARCIAL | Fila HTTP resumível para arquivos diretos reconhecidos e playback offline; não empacota HLS/DASH segmentado. Serviço ainda precisa teste em Android real. |
| Edição e exportação M3U | PARCIAL | Edita nome/grupo e compartilha exportação das entradas carregadas; não é editor avançado nem preserva todas extensões. |
| Backup e restauração | IMPLEMENTADO NO CÓDIGO | Formato próprio cifrado com frase secreta e limite de tamanho; teste instrumentado escrito, não executado. |
| Preferências e persistência | PARCIAL | Autoplay/PiP, conteúdo local e migração antiga; sem sincronização entre dispositivos. |
| Tratamento de rede | PARCIAL | Timeouts/limites HTTP e erros sanitizados no Xtream; sem política de retry global. |
| Proteção de credenciais | IMPLEMENTADO NO CÓDIGO | AES-GCM Android Keystore para dados locais; backup exportado recebe cifra PBKDF2/AES-GCM. |
| Android TV/D-pad | PARCIAL | Leanback launcher e widgets focáveis; não validado com controle/televisão física. |
