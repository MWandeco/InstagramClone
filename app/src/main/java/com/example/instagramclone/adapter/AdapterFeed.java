package com.example.instagramclone.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.activity.ComentariosActivity;
import com.example.instagramclone.helper.ConfiguracaoFirebase;
import com.example.instagramclone.helper.UsuarioFirebase;
import com.example.instagramclone.model.Feed;
import com.example.instagramclone.model.PostagemCurtida;
import com.example.instagramclone.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFeed extends RecyclerView.Adapter<AdapterFeed.MyViewHolder> {

    private List<Feed> listaFeed;
    private Context context;

    public AdapterFeed(List<Feed> listaFeed, Context context) {
        this.listaFeed = listaFeed;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_feed, parent, false);
        return new AdapterFeed.MyViewHolder(itemLista);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final Feed feed = listaFeed.get(position);
        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //Carrega dados do feed
        Uri uriFotoUsuario = Uri.parse(String.valueOf(feed.getFotoUsuario()));
        Uri uriFotoPostagem = Uri.parse(String.valueOf(feed.getFotoPostagem()));

        Glide.with( context ).load( uriFotoUsuario ).into(holder.fotoPerfil);
        Glide.with( context ).load( uriFotoPostagem ).into(holder.fotoPostagem);

        holder.descricao.setText( feed.getDescricao() );
        holder.nome.setText( feed.getNomeUsuario() );

        //recuperar dados da postagem curtida
        DatabaseReference curtidasRef = ConfiguracaoFirebase
                .getFirebase()
                .child("postagens-curtidas")
                .child(feed.getId());

        //evento de clique dos comentários
        holder.visualizarComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ComentariosActivity.class);
                i.putExtra("idPostagem",feed.getId());
                context.startActivity(i);
            }
        });

        curtidasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int qtdCurtidas = 0 ;
                if(snapshot.hasChild("qtdCurtidas")){
                    PostagemCurtida postagemCurtida = snapshot.getValue(PostagemCurtida.class);
                    qtdCurtidas = postagemCurtida.getQtdCurtidas();
                }

                //monta o objeto curtida
                PostagemCurtida curtida = new PostagemCurtida();
                curtida.setFeed(feed);
                curtida.setUsuario(usuarioLogado);
                curtida.setQtdCurtidas(qtdCurtidas);

                //verifica se a postagem já foi curtida
                if(snapshot.hasChild(usuarioLogado.getId())){
                    Drawable drawable = ContextCompat.getDrawable(context,R.drawable.ic_heart_vermelho);
                    holder.likeButton.setImageDrawable(drawable);
                    holder.qtdCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
                }else{
                    Drawable drawable = ContextCompat.getDrawable(context,R.drawable.ic_heart_cinza);
                    holder.likeButton.setImageDrawable(drawable);
                    holder.qtdCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
                }

                //adicionar evento ao curtir uma foto
                holder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Drawable drawable = ContextCompat.getDrawable(context,R.drawable.ic_heart_vermelho);
                        holder.likeButton.setImageDrawable(drawable);
                        curtida.salvar();
                        holder.qtdCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
                    }
                });
                holder.likeButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Drawable drawable = ContextCompat.getDrawable(context,R.drawable.ic_heart_cinza);
                        curtida.removerCurtida();
                        holder.likeButton.setImageDrawable(drawable);
                        holder.qtdCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
                        return false;
                    }
                });
                holder.qtdCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    @Override
    public int getItemCount() {
        return listaFeed.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView fotoPerfil;
        TextView nome, descricao, qtdCurtidas;
        ImageView fotoPostagem, visualizarComentario;
        ImageButton likeButton;

        public MyViewHolder(View itemView) {
            super(itemView);

            //TODO arrumar um botao de like que funcione

            fotoPerfil   = itemView.findViewById(R.id.imagePerfilPostagem);
            fotoPostagem = itemView.findViewById(R.id.imagemPostagemSelecionada);
            nome         = itemView.findViewById(R.id.textPerfilPostagem);
            qtdCurtidas  = itemView.findViewById(R.id.textQuantidadeCurtidas);
            descricao    = itemView.findViewById(R.id.textDescricaoPostagem);
            visualizarComentario    = itemView.findViewById(R.id.imageComentarioFeed);
            likeButton = itemView.findViewById(R.id.likeButtonFeed);
        }
    }

}
