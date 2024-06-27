{
  description = "AST Merge Driver";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-24.05-darwin";
  };

  outputs = { self, nixpkgs }:
    let
      pkgs = nixpkgs.legacyPackages.aarch64-darwin.pkgs;
    in
    {
      devShells.aarch64-darwin.default = pkgs.mkShell {
        buildInputs = with pkgs;[
          zulu
        ];
      };
    };
}
