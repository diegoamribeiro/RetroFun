#!/bin/bash
# Script para limpar completamente o cache do Xcode

echo "🧹 Limpando cache do Xcode..."

# 1. Fechar Xcode se estiver aberto
killall Xcode 2>/dev/null || true
sleep 2

# 2. Remover DerivedData
echo "Removendo DerivedData..."
rm -rf ~/Library/Developer/Xcode/DerivedData/iosApp-*

# 3. Remover caches do projeto
echo "Removendo caches do projeto..."
rm -rf iosApp/iosApp.xcodeproj/xcuserdata
rm -rf iosApp/iosApp.xcodeproj/project.xcworkspace/xcuserdata

# 4. Remover build do projeto
echo "Removendo build..."
rm -rf iosApp/build

echo "✅ Cache limpo!"
echo ""
echo "Agora:"
echo "1. Abra o Xcode"
echo "2. Pressione Shift+Cmd+K (Product > Clean Build Folder)"
echo "3. Pressione Cmd+B para buildar novamente"
