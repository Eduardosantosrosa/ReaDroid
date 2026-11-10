# Telas e navegação

## Original

Rotas mobile/desktop em `src/pages`: `/`, `/login`, `/livetv`, `/movies`, `/movies/detail`, `/series`, `/series/detail`, `/epg`, `/search`, `/favorites`, `/watchlist`, `/recently-added`, `/downloads`, `/settings`, `/playlist-editor`, `/receiver` e `/docs`. Rotas `/tv/*` e `TvLayout.astro` oferecem shell e telas de TV com maior foco em controle remoto.

Fluxos evidenciados: usuário sem fonte chega ao onboarding/login e adiciona Xtream ou URL M3U; depois a navegação lateral leva a live, VOD, EPG e biblioteca; cartões de filmes/séries abrem detalhe; seleção no player inicia mídia. O switcher e login suportam múltiplas fontes. Há páginas de downloads e edição.

## Reconstrução

1. Home simples com abas TV ao vivo, Filmes, Séries, Favoritos e Adicionar.
2. Busca textual restringe entradas carregadas.
3. Categoria muda o conjunto visível; em Xtream o catálogo é consultado novamente.
4. Linha abre playback; estrela grava/remove favorito.
5. Adicionar pede URL M3U ou host/usuário/senha Xtream. Mensagens informam erro de rede e importação.
6. Player fullscreen mantém tela ativa e oferece controles Media3.

Critérios básicos de validação ainda não executados: foco inicial visível; D-pad alcança todos os controles; retorno mantém fonte/categoria; filtros não misturam tipos; rotação respeita navegação; diálogo de credenciais não expõe senha. O layout ainda precisa de trabalho específico para TV e acessibilidade.
