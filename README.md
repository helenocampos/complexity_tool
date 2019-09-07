# Complexity Tool

This project is a reserach product developed at IF Sudeste MG since 2014. The main research goal is to study and develop techniques related to program structural complexity and assess their impact on unit tests.
In particular, the software from this respository has the following features:
- Plot the control flow graph of a method based on a given Java source code.
- Calculate the cyclomatic complexity based on the plotted graph.
- Calculate all independent paths in the graph, which should be tested by unit tests.
- Check if the source code can be written with less cyclomatic complexity without compromising its functionality. We call this *unnecessary cyclomatic complexity*.
- Plot a control flow graph representing the optimized source code without unnecessary cyclomatic complexity.
- Generate an optimized version of the source code, without the unnecessary cyclomatic complexity, when applicable.
- Generate unit tests templates based on this whole analysis.

People that directly contributted to the development of this project:
- Marco Antônio Pereira Araújo (academic advisor, 2014-today)
- Heleno de Souza Campos Junior (Lead developer and main investigator, 2014-today)
- Nathan Manera Magalhães (Developer and investigator, 2016-today)
- Gabriel Felix Vaneli (Developer, 2018-2018)
- Luis Rogério Ventura Martins Filho (Developer, 2015-2016)
- Alisson Fernandes do Prado (Developer, 2016-2016)

This research was financially supported by FAPEMIG, IF Sudeste MG and CNPq.

Publications related to this project:
- [Vaneli, G. F., & Araújo, M. A. P. (2018). Uma Abordagem para Geração Automatizada de Testes de Unidade em Código Fonte sem Complexidade Desnecessária. Seminários de Trabalho de Conclusão de Curso do Bacharelado em Sistemas de Informação, 3(1). (PORTUGUESE)](http://periodicos.jf.ifsudestemg.edu.br/revistabsi/article/view/247/87)
- [MAGALHAES, N. M. ; CAMPOS JUNIOR, H. S. ; ARAUJO, M. A. P. . Melhoria da qualidade de software através da eliminação da complexidade desnecessária em código fonte. Multiverso: Revista Eletrônica do Campus Juiz de Fora - IF Sudeste MG, v. 3, p. 1, 2018. (PORTUGUESE)](http://periodicos.jf.ifsudestemg.edu.br/multiverso/article/view/223/100)
- [MAGALHAES, N. M. ; CAMPOS JUNIOR, H. S. ; ARAUJO, M. A. P. ; NEVES, V. O. . An Automated Refactoring Approach to Remove Unnecessary Complexity in Source Code. In: 2nd Brazilian Symposium on Systematic and Automated Software Testing, 2017, Fortaleza. Proceedings of the 2nd Brazilian Symposium on Systematic and Automated Software Testing. New Tork: ACM, 2017. (PORTUGUESE)](https://dl.acm.org/citation.cfm?id=3128476)
- MAGALHAES, N. M. ; CAMPOS JUNIOR, H. S. ; ARAUJO, M. A. P. . Melhoria da Qualidade de Software através da Eliminação da Complexidade Desnecessária em Código Fonte. In: Reunião Anual da SBPC, 2017, Belo Horizonte. Anais da 69a. Reunião Anual da SBPC, 2017. (PORTUGUESE)
- [CAMPOS JUNIOR, H. S.; MARTINS FILHO, L. R. V. ; ARAUJO, M. A. P. . Uma abordagem para otimização da qualidade de código fonte baseado na complexidade estrutural. Multiverso: Revista Eletrônica do campus Juiz de Fora, v. 2, p. 13-21, 2017. (PORTUGUESE)](http://periodicos.jf.ifsudestemg.edu.br/multiverso/article/view/85/60)
- [CAMPOS JUNIOR, H. S.; MARTINS FILHO, L. R. ; ARAÚJO, MARCO ANTÔNIO PEREIRA . An Approach for Detecting Unnecessary Cyclomatic Complexity on Source Code. Revista IEEE América Latina, v. 14, p. 3777-3783, 2016. (PORTUGUESE)](https://ieeexplore.ieee.org/document/7786363)
- [CAMPOS JUNIOR, H. S.; PRADO, A. F. ; ARAÚJO, MARCO ANTÔNIO PEREIRA . Complexity Tool: Uma Ferramenta para Medir Complexidade Ciclomática de Métodos Java. Multiverso: Revista Eletrônica do Campus Juiz de Fora - IF Sudeste MG, v. 1, p. 66-76, 2016. (PORTUGUESE)](http://periodicos.jf.ifsudestemg.edu.br/multiverso/article/view/9/8)
- CAMPOS JUNIOR, H. S.; MARTINS FILHO, L. R. V. ; ARAUJO, M. A. P. . Uma Abordagem para Otimização da Qualidade de Código Fonte Baseado na Complexidade Estrutural. In: Jornada Nacional de Iniciação Científica - 68a. Reunião Anual da SBPC, 2016, Porto Seguro/BA. Anais da Jornada Nacional de Iniciação Científica - 68a. Reunião Anual da SBPC 2016, 2016. (PORTUGUESE)
- [CAMPOS JUNIOR, H. S.; MARTINS FILHO, L. R. V. ; ARAUJO, M. A. P. . Uma ferramenta interativa para visualização de código fonte no apoio à construção de casos de teste unitário. In: BRAZILIAN WORKSHOP ON SYSTEMATIC AND AUTOMATED SOFTWARE TESTING, 2015, Belo Horizonte. Proceedings of the BRAZILIAN WORKSHOP ON SYSTEMATIC AND AUTOMATED SOFTWARE TESTING, 2015. (PORTUGUESE)](https://www.researchgate.net/publication/327681388_Uma_ferramenta_interativa_para_visualizacao_de_codigo_fonte_no_apoio_a_construcao_de_casos_de_teste_de_unidade)
- MARTINS FILHO, L. R. V. ; CAMPOS JUNIOR, H. S. ; ARAUJO, M. A. P. . Uma Ferramenta de Apoio à Construção de Teste de Unidade através da Geração de Grafos de Fluxo de Controle. In: II SIMEPE - Simpósio de Ensino, Pesquisa e Extensão do IF Sudeste MG, 2015, Barbacena. Anais do II SIMEPE, 2015. (PORTUGUESE)
- CAMPOS JUNIOR, H. S.; MARTINS FILHO, L. R. V. ; ARAUJO, M. A. P. . UMA ABORDAGEM PARA OTIMIZAÇÃO DA QUALIDADE DE CÓDIGO FONTE BASEADO NA COMPLEXIDADE ESTRUTURAL. In: V Seminário de Iniciação Científica do IF Sudeste MG Campus Juiz de Fora, 2015, Juiz de Fora. Anais do V Seminário de Iniciação Científica do IF Sudeste MG Campus Juiz de Fora, 2015. (PORTUGUESE)

- [Pitch do projeto para a Mostra Fapemig 2015. (Video in PORTUGUESE)](https://www.youtube.com/watch?v=cCPu1H8-Apk)

# How to compile
Clone the repository
```
git clone https://github.com/helenocampos/complexity_tool
```

Enter directory and compile with Maven
```
cd complexity_tool
mvn install
```

# How to execute the GUI
Execute the jar with dependencies that is generated in the target folder or download the jar from the releases page.



[Research group website (outdated)](https://esifjf.github.io)
