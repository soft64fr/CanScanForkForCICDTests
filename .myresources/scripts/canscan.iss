; ============================================================================
; CanScan - Inno Setup Edition Installation Script
; Architecture: x64 only
; ============================================================================

; Définitions par défaut (peuvent être surchargées par /D en ligne de commande)
#ifndef AppName
  #define AppName "CanScan"
#endif

#ifndef AppVersion
  #define AppVersion "0.0.0.0"
#endif

#ifndef Organization
  #define Organization "Soft64.fr"
#endif

[Setup]
; Informations de l'application
AppId={{AA74CA9F-E961-4A51-A0CB-228B63029F74}}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#Organization}
AppPublisherURL=https://{#Organization}
AppSupportURL=https://{#Organization}
AppUpdatesURL=https://github.com/Lob2018/{#AppName}/releases/latest
VersionInfoVersion={#AppVersion}.0
VersionInfoCompany={#Organization}
VersionInfoDescription={#AppName}
VersionInfoCopyright=Copyright (C) 2025 {#Organization}

; Répertoires d'installation
PrivilegesRequired=lowest
DefaultDirName={commonpf}\{#Organization}\{#AppName}
DefaultGroupName={#Organization}\{#AppName}
DisableProgramGroupPage=yes

; Répertoires de sortie
OutputDir=../../output
OutputBaseFilename={#AppName}-{#AppVersion}-x64

; Compression
Compression=lzma2/max
SolidCompression=no

; Architecture
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible

; Interface utilisateur
WizardStyle=modern
SetupIconFile=../images/{#AppName}.ico
UninstallDisplayIcon={app}\{#AppName}.ico

; Désinstallation
UninstallDisplayName={#AppName}
CreateUninstallRegKey=yes

; Divers
AllowNoIcons=yes
DisableWelcomePage=no

[Languages]
Name: "french"; MessagesFile: "compiler:Languages\French.isl"

[Files]
; Application principale
Source: "../../dist/{#AppName}-{#AppVersion}.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "../images/{#AppName}.ico"; DestDir: "{app}"; Flags: ignoreversion

; DLLs
Source: "../../dist/*.dll"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs

; Ressources (si nécessaire)
; Source: "../../dist/resources/*"; DestDir: "{app}\resources"; Flags: ignoreversion recursesubdirs

; Visual C++ Redistributable
Source: "../VCRUNTIME/VC_redist.x64.exe"; DestDir: "{tmp}"; Flags: deleteafterinstall

; Fichiers de documentation (optionnel)
Source: "../../README.md"; DestDir: "{app}"; DestName: "README.txt"; Flags: ignoreversion
Source: "../../LICENSE.txt"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
; Menu Démarrer
Name: "{group}\{#AppName}"; Filename: "{app}\{#AppName}-{#AppVersion}.exe"; IconFilename: "{app}\{#AppName}.ico"; Comment: "{#AppName}"
Name: "{group}\Désinstaller {#AppName}"; Filename: "{uninstallexe}"; Comment: "Désinstaller {#AppName}"

; Bureau (optionnel)
Name: "{userdesktop}\{#AppName}"; Filename: "{app}\{#AppName}-{#AppVersion}.exe"; IconFilename: "{app}\{#AppName}.ico"; Comment: "{#AppName}"; Tasks: desktopicon

[Tasks]
Name: "desktopicon"; Description: "Ajouter un raccourci sur le bureau"; GroupDescription: "Raccourcis :"; Flags: unchecked

[Run]
; Lancer l'application après installation (optionnel mais coché)
Filename: "{app}\{#AppName}-{#AppVersion}.exe"; Description: "Lancer {#AppName}"; Flags: nowait postinstall skipifsilent

; Afficher le fichier README (optionnel non coché)
Filename: "{app}\README.txt"; Description: "Afficher le fichier README"; Flags: postinstall skipifsilent unchecked

[UninstallDelete]
; Supprimer les données locales
Type: filesandordirs; Name: "{localappdata}\{#Organization}\{#AppName}"
; Supprimer le dossier d'installation principal
Type: filesandordirs; Name: "{app}"
; Supprimer le dossier {#Organization} si vide
Type: dirifempty; Name: "{commonpf}\{#Organization}"
Type: dirifempty; Name: "{localappdata}\{#Organization}"

[Code]
var
  VCInstallPage: TOutputProgressWizardPage;

// ============================================================================
// Vérification du Visual C++ Redistributable
// ============================================================================
function NeedsVC: Boolean;
var
  Major: Cardinal;
begin
  // Vérifie si VCRUNTIME140_1.dll existe et sa version
  Result := not RegQueryDWordValue(HKLM,
    'SOFTWARE\Microsoft\VisualStudio\14.0\VC\Runtimes\x64',
    'Major', Major) or (Major < 14);

  // Fallback: vérifier la présence du fichier DLL
  if not Result then
    Result := not FileExists(ExpandConstant('{sys}\VCRUNTIME140_1.dll'));
end;

// ============================================================================
// Création de la page d'installation de VC++
// ============================================================================
procedure InitializeWizard();
begin
  VCInstallPage := CreateOutputProgressPage(
    'Installation des composants requis',
    'Veuillez patienter pendant l''installation des composants requis');
end;

// ============================================================================
// Installation de VC++ AVANT la copie des fichiers de {#AppName}
// ============================================================================
function PrepareToInstall(var NeedsRestart: Boolean): String;
var
  ResultCode: Integer;
  VCRedistPath: String;
begin
  Result := '';

  if NeedsVC then
  begin
    VCRedistPath := ExpandConstant('{tmp}\VC_redist.x64.exe');

    // Extraire le fichier VC_redist.x64.exe dans {tmp} si ce n'est pas déjà fait
    ExtractTemporaryFile('VC_redist.x64.exe');

    // Afficher la page personnalisée
    VCInstallPage.SetText(
      'Installation de Visual C++ Redistributable v14.44.35211 en cours...',
      'Cela peut prendre quelques minutes');
    VCInstallPage.Show;

    try
      // Démarrer la barre de progression
      VCInstallPage.SetProgress(0, 100);
      VCInstallPage.ProgressBar.Style := npbstMarquee;

      // Exécuter l'installation VC++ et attendre la fin
      if not Exec(VCRedistPath, '/quiet /norestart', '', SW_HIDE,
                  ewWaitUntilTerminated, ResultCode) then
      begin
        Result := 'Erreur lors de l''installation de Visual C++ Redistributable.' + #13#10 +
                  'Impossible de lancer l''installateur.';
        Exit;
      end;

      // Vérifier si un redémarrage est nécessaire (code 3010)
      if ResultCode = 3010 then
        NeedsRestart := True;

      // Vérifier si l'installation a échoué
      if (ResultCode <> 0) and (ResultCode <> 3010) then
      begin
        Result := 'L''installation de Visual C++ Redistributable a échoué.' + #13#10 +
                  'Code d''erreur: ' + IntToStr(ResultCode) + #13#10#13#10 +
                  'L''installation de {#AppName} va continuer, mais l''application pourrait ne pas fonctionner correctement.';
        // On continue quand même l'installation
        Result := '';
      end;

      VCInstallPage.SetProgress(100, 100);

    finally
      VCInstallPage.Hide;
    end;
  end;
end;

// ============================================================================
// Fonction appelée avant l'installation
// ============================================================================
function InitializeSetup(): Boolean;
begin
  Result := True;
  // Vérifier si l'architecture est compatible
  if not Is64BitInstallMode then
  begin
    MsgBox('Cette application requiert un système Windows 64 bits.',
      mbError, MB_OK);
    Result := False;
  end;
end;