@echo off
setlocal
cls
:: --- CABECALHO ---
echo.
echo     ==================================
echo          Estrutura de Dados II
echo          Prof. Dr. Jean M. Laine
echo                Projeto 1
echo     ==================================
echo.
pause

echo --- LIMPANDO ARQUIVOS ANTIGOS ---
del *.class
del huffman.jar
del teste.huff
del teste_restaurado.txt

echo.
echo --- COMPILANDO ARQUIVOS JAVA ---
javac Huffman.java No.java MinHeap.java
if errorlevel 1 (
    echo.
    echo *** ERRO NA COMPILACAO! ***
    pause
    exit /b
)

echo.
echo --- CRIANDO O ARQUIVO JAR ---
jar cfe huffman.jar Huffman *.class

echo.
echo --- EXECUTANDO PROGRAMA ---
echo --- Comprimindo arq_de_teste.txt...
java -jar huffman.jar -c arq_de_teste.txt teste.huff

echo.
echo.
echo.
pause
echo.
echo --- Descomprimindo teste.huff...
echo.
java -jar huffman.jar -d teste.huff teste_restaurado.txt

echo.
echo --- VERIFICANDO RESULTADO ---
echo.
fc /B arq_de_teste.txt teste_restaurado.txt > nul
if errorlevel 1 ( 
	echo *** FALHA: O arquivo original e o restaurado sao DIFERENTES. ***
) else (
	echo *** SUCESSO: O arquivo original e o restaurado sao IDENTICOS. ***
)

echo.
echo Limpando arquivos temporarios...
del teste.huff
del teste_restaurado.txt

echo.