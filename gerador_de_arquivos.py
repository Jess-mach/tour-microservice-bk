import os

def criar_arquivos_com_conteudo(caminho_arquivo_entrada):
    """
    Lê um arquivo de entrada, procura por linhas que começam com '//',
    cria arquivos com base nos caminhos e preenche-os com o conteúdo
    que segue, até a próxima linha '//' ou o final do arquivo.

    Args:
        caminho_arquivo_entrada (str): O caminho para o arquivo de texto de entrada.
    """
    try:
        with open(caminho_arquivo_entrada, 'r', encoding='utf-8') as f:
            caminho_atual_arquivo = None
            conteudo_atual = []

            for numero_linha, linha in enumerate(f, 1):
                linha_limpa = linha.strip()

                if linha.rstrip('\n\r').startswith('//'):
                    # Se estamos processando um arquivo anterior, salvamos o conteúdo
                    if caminho_atual_arquivo:
                        salvar_arquivo(caminho_atual_arquivo, "".join(conteudo_atual))
                        conteudo_atual = [] # Reseta o conteúdo para o próximo arquivo

                    caminho_relativo = linha_limpa[2:].strip() # Remove '//' e espaços

                    if not caminho_relativo:
                        print(f"Aviso: Linha {numero_linha} começa com '//' mas não tem um caminho válido. Ignorando.")
                        caminho_atual_arquivo = None # Impede que conteúdo seja salvo em um caminho inválido
                        continue

                    # Extrai o diretório e o nome do arquivo
                    diretorio = os.path.dirname(caminho_relativo)
                    nome_arquivo = os.path.basename(caminho_relativo)

                    if diretorio: # Se houver subpastas, cria-as
                        os.makedirs(diretorio, exist_ok=True)
                        print(f"Diretório(s) '{diretorio}' criado(s) ou já existente(s).")

                    caminho_atual_arquivo = os.path.join(diretorio, nome_arquivo)
                    print(f"Detectado novo arquivo: '{caminho_atual_arquivo}'")

                elif caminho_atual_arquivo:
                    # Adiciona a linha ao conteúdo do arquivo atual, mantendo a quebra de linha original
                    conteudo_atual.append(linha)
                else:
                    # Ignora linhas antes do primeiro '//' ou se um caminho inválido foi encontrado
                    if linha_limpa: # Apenas imprime se a linha não estiver vazia
                        print(f"Linha {numero_linha} ignorada (não associada a um caminho ou antes do primeiro '//'): '{linha_limpa}'")

            # Salva o conteúdo do último arquivo, se houver
            if caminho_atual_arquivo and conteudo_atual:
                salvar_arquivo(caminho_atual_arquivo, "".join(conteudo_atual))

    except FileNotFoundError:
        print(f"Erro: O arquivo de entrada '{caminho_arquivo_entrada}' não foi encontrado.")
    except Exception as e:
        print(f"Ocorreu um erro inesperado: {e}")

def salvar_arquivo(caminho_completo, conteudo):
    """
    Função auxiliar para salvar o conteúdo em um arquivo.
    Se o arquivo já existir, ele será removido e recriado com o novo conteúdo.
    """
    try:
        # Verifica se o arquivo já existe para emitir uma mensagem específica
        if os.path.exists(caminho_completo):
            print(f"Arquivo '{caminho_completo}' já existe. Removendo e recriando...")
            os.remove(caminho_completo) # Remove o arquivo existente

        # Abre o arquivo no modo de escrita ('w').
        # Se o arquivo foi removido, ele será criado. Se não existia, será criado.
        with open(caminho_completo, 'w', encoding='utf-8') as f:
            f.write(conteudo)
        print(f"Conteúdo salvo em '{caminho_completo}'.")
    except IOError as e:
        print(f"Erro ao salvar o arquivo '{caminho_completo}': {e}")
    except OSError as e: # Adicionado para capturar erros específicos de remoção de arquivo
        print(f"Erro ao remover o arquivo '{caminho_completo}': {e}")

# --- Exemplo de Uso ---
if __name__ == "__main__":
    # O arquivo de teste será o 'meu_arquivo_de_classes.txt' que você forneceu.
    # Certifique-se de que ele esteja no mesmo diretório do script Python.

    nome_arquivo_entrada = "meu_arquivo_de_classes.txt"

    print(f"Iniciando o processamento do arquivo: '{nome_arquivo_entrada}'\n")
    criar_arquivos_com_conteudo(nome_arquivo_entrada)
    print("\n--- Processo Concluído ---")
    print(f"Verifique os arquivos e diretórios criados no mesmo diretório de '{nome_arquivo_entrada}'.")