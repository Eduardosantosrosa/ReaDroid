# Reconstrução Android independente

Projeto Android Java independente baseado em análise estática documentada da referência. Não reutiliza código, imagens, fontes, ícones ou identidade visual do produto original. Nome provisório: **ReaDroid Stream**. Licença deste código novo: MIT; a licença GPL do projeto de referência não é alterada.

## Estado verificado

Implementação parcial: uma fonte Xtream ou URL M3U remota, consultas/listagem básicas, filtros/pesquisa locais, favoritos em SharedPreferences e player Media3. Para Xtream, séries agora consultam `get_series_info`, oferecem episódios por temporada e iniciam a reprodução. EPG, múltiplas fontes, biblioteca/histórico, downloads, editor e backup ainda não estão implementados. A senha é guardada sem cifragem adicional. Os quatro testes JUnit do parser estão escritos; execução e novo build seguem pendentes neste ambiente. Não há APK gerado localmente. Consulte `STATUS_REAL_DO_PROJETO.md` e as matrizes.

## Build

Consulte `INSTRUCOES_DE_COMPILACAO.md`. Build configurado para AGP 8.6.1, SDK 35, Java 17 e Gradle 8.7 pelo wrapper. O workflow do GitHub Actions executa JUnit e produz APK Debug; também produz Release assinado se os secrets forem configurados. O Termux inspecionado não tem Android SDK, portanto não declare compilação local até obter evidência real.

Nenhuma credencial ou playlist real deve ser adicionada ao repositório. A conexão vai diretamente ao host informado pelo usuário. Veja `LIMITACOES_E_PENDENCIAS.md` antes de distribuir.
