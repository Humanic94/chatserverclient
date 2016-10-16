package no.ntnu.tollefsen.crazychat;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.SimpleDateFormat;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import no.ntnu.tollefsen.crazychat.domain.Conversation;
import no.ntnu.tollefsen.crazychat.domain.Message;
import no.ntnu.tollefsen.crazychat.domain.MessageException;
import no.ntnu.tollefsen.crazychat.domain.User;


/**
 * Created by mikael on 26.09.16.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    static SimpleDateFormat SDF = new SimpleDateFormat("EEE HH:mm");

    public interface OnClickListener {
        void onClick(int position);
    }

    Context context;
    List<Conversation> conversations;
    OnClickListener listener = position -> {};
    Transformation rounded = new CropCircleTransformation();

    public ConversationAdapter(Context context, @NonNull List<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.conversation_row, parent, false);
        return new ConversationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        if(conversation.getMessageCount() > 0) {
            try {
                Message message = conversation.getLastMessage();
                holder.message.setText(message.getText());
                holder.time.setText(message.getTimestamp() != null ? SDF.format(message.getTimestamp()) : "13:37");

                List<User> recipients = conversation.getRecipients();
                User recipient = recipients.size() > 0 ? recipients.get(0) : null;
                if(recipient != null && recipient.getPhotoURI() != null) {
                    Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, recipient.getUid());
                    Picasso.with(context).load(contactUri).fit().transform(rounded).into(holder.thumb);
                } else {
                    String name = recipient != null ? recipient.getName() : "A";
                    String str = name.length() > 0 ? name.substring(0,1) : "A";
                    TextDrawable drawable = TextDrawable.builder().buildRound(str, ColorGenerator.MATERIAL.getColor(name));
                    holder.thumb.setImageDrawable(drawable);
                }
            } catch (MessageException e) {
                Log.i("Conversation","Illegal message access");
            }
        }
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public Conversation getItem(int position) {
        return conversations.get(position);
    }
    public void setItems(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public List<Conversation> getItems() {
        return conversations;
    }

    public void setOnClickListener(@NonNull OnClickListener listener) {
        this.listener = listener;
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder {
        public TextView time;
        public TextView message;
        public ImageView thumb;

        public ConversationViewHolder(View view) {
            super(view);
            view.setOnClickListener(v -> listener.onClick(getAdapterPosition()));
            this.time = (TextView)view.findViewById(R.id.time);
            this.message = (TextView)view.findViewById(R.id.message);
            this.thumb = (ImageView) view.findViewById(R.id.thumb);
        }
    }
}
