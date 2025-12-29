import os

IOS_APP_DIR = "iosApp/iosApp"

# Extensões de arquivos para MANTER
KEEP_EXTENSIONS = {'.c', '.cpp', '.h', '.hpp', '.cc', '.cxx', '.hxx'}

# Nomes de arquivos específicos para REMOVER
JUNK_NAMES = {
    'CMakeLists.txt', 'Makefile', 'LICENSE', 'LICENSE.txt', 'README', 'README.md',
    'readme.txt', 'make_vms.com', '.put_lib_files_here', '.gitignore', '.gitkeep'
}

# Extensões de arquivos para REMOVER
JUNK_EXTENSIONS = {
    '.vcxproj', '.sln', '.bor', '.emx', '.msc', '.def', '.rc', '.com', '.md', '.txt',
    '.cmake', '.in', '.pc', '.am', '.ac', '.m4', '.sh', '.bat', '.ps1'
}

def should_remove(filename):
    """Determina se um arquivo deve ser removido."""
    # Nomes específicos
    if filename in JUNK_NAMES:
        return True
    
    # Extensão do arquivo
    _, ext = os.path.splitext(filename)
    ext_lower = ext.lower()
    
    # Se tem extensão de código, MANTER
    if ext_lower in KEEP_EXTENSIONS:
        return False
    
    # Se tem extensão de lixo, REMOVER
    if ext_lower in JUNK_EXTENSIONS:
        return True
    
    # Arquivos sem extensão reconhecida mas com nomes suspeitos
    if any(junk in filename.lower() for junk in ['makefile', 'readme', 'license', 'changelog', 'todo', 'authors']):
        return True
    
    return False

def clean_directory(directory):
    """Remove recursivamente arquivos indesejados."""
    removed_count = 0
    
    for root, dirs, files in os.walk(directory):
        for filename in files:
            if should_remove(filename):
                filepath = os.path.join(root, filename)
                try:
                    os.remove(filepath)
                    print(f"Removido: {os.path.relpath(filepath, directory)}")
                    removed_count += 1
                except Exception as e:
                    print(f"Erro ao remover {filepath}: {e}")
    
    return removed_count

def main():
    print("Limpando arquivos desnecessários em iosApp/iosApp...")
    print("Mantendo apenas: .c, .cpp, .h, .hpp")
    print()
    
    # Limpar genesis_plus_gx
    genesis_dir = os.path.join(IOS_APP_DIR, "genesis_plus_gx")
    if os.path.exists(genesis_dir):
        print(f"Limpando {genesis_dir}...")
        count = clean_directory(genesis_dir)
        print(f"✓ Removidos {count} arquivos de genesis_plus_gx\n")
    
    # Limpar laines
    laines_dir = os.path.join(IOS_APP_DIR, "laines")
    if os.path.exists(laines_dir):
        print(f"Limpando {laines_dir}...")
        count = clean_directory(laines_dir)
        print(f"✓ Removidos {count} arquivos de laines\n")
    
    print("Limpeza concluída! ✨")

if __name__ == "__main__":
    main()
