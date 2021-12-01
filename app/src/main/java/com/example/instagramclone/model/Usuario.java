package com.example.instagramclone.model;

import com.example.instagramclone.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable {
    private String id;
    private String nome;
    private String email;
    private String senha;
    private String caminhoFoto;
    private int Seguidores = 0;
    private int Seguindo = 0;
    private int postagens = 0;

    public int getSeguidores() {
        return Seguidores;
    }

    public void setSeguidores(int seguidores) {
        Seguidores = seguidores;
    }

    public int getSeguindo() {
        return Seguindo;
    }

    public void setSeguindo(int seguindo) {
        Seguindo = seguindo;
    }

    public int getPostagens() {
        return postagens;
    }

    public void setPostagens(int postagens) {
        this.postagens = postagens;
    }

    public Usuario() {
    }
     public void salvar(){
         DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
         DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(getId());
         usuariosRef.setValue(this);
     }
     public void atualizar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(getId());

        Map<String,Object> valoresUsuario = converterParaMap();
        usuarioRef.updateChildren(valoresUsuario);
     }
     public void atualizarQtdPostagem(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        Map objeto = new HashMap();
        objeto.put("/usuarios/" + getId() + "/nome",getNome() );
        objeto.put("/usuarios/" + getId() + "/caminhoFoto",getCaminhoFoto() );

        firebaseRef.updateChildren(objeto);

     }
     public Map<String,Object> converterParaMap(){
         HashMap<String,Object> usuarioMap = new HashMap<>();
         usuarioMap.put("email",getEmail());
         usuarioMap.put("nome",getNome());
         usuarioMap.put("id",getId());
         usuarioMap.put("caminhoFoto",getCaminhoFoto());
         usuarioMap.put("seguidores",getSeguidores());
         usuarioMap.put("seguindo",getSeguindo());
         usuarioMap.put("postagens",getPostagens());

         return usuarioMap;
     }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {

        this.nome = nome.toUpperCase();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }
}
