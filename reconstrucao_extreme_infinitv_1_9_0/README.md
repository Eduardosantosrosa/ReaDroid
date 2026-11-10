# Reconstrução Android independente

Projeto Android Java independente baseado em análise estática documentada da referência. Não reutiliza código, imagens, fontes, ícones ou identidade visual do produto original. Nome provisório: **ReaDroid Stream**. Licença deste código novo: MIT; a licença GPL do projeto de referência não é alterada.

## Estado verificado

Implementação parcial: uma fonte Xtream ou URL M3U remota, consultas/listagem básicas, filtros/pesquisa locais, favoritos em SharedPreferences e player Media3. Séries não abrem detalhes nem episódios; EPG, múltiplas fontes, biblioteca/histórico, downloads, editor e backup não estão implementados. A senha é guardada sem cifragem adicional. Os quatro testes JUnit do parser estão escritos, porém não executados nesta auditoria. Não há APK gerado. Consulte `STATUS_REAL_DO_PROJETO.md` e as matrizes.

## Build

Consulte `INSTRUCOES_DE_COMPILACAO.md`. Build configurado para AGP 8.6.1, SDK 35, Java 17 e Gradle 8.7. O Termux inspecionado não tem Android SDK nem o plugin configurado no cache; não declare compilação até obter evidência de build real.

Nenhuma credencial ou playlist real deve ser adicionada ao repositório. A conexão vai diretamente ao host informado pelo usuário. Veja `LIMITACOES_E_PENDENCIAS.md` antes de distribuir.
