package no.ntnu.tollefsen.crazychat;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import no.ntnu.tollefsen.crazychat.domain.Message;
import no.ntnu.tollefsen.crazychat.domain.MessageService;
import no.ntnu.tollefsen.crazychat.domain.User;

/**
 * Created by mikael on 26.09.16.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    static final int USER_MESSAGE = 0;
    static final int RECIPIENT_MESSAGE = 1;

    static final int DEFAULT_RIGHT_LAYOUT = R.layout.message_row_right;
    static final int DEFAULT_LEFT_LAYOUT  = R.layout.message_row_left;

    static SimpleDateFormat SDF = new SimpleDateFormat("EEE HH:mm");

    int layoutLeft  = DEFAULT_LEFT_LAYOUT;
    int layoutRight = DEFAULT_RIGHT_LAYOUT;


    public interface OnClickListener {
        void onClick(int position);
    }


    List<Message> messages;
    OnClickListener listener = position -> {};

    Context context;
    Transformation rounded = new CropCircleTransformation();

    public MessageAdapter(@NonNull Context context) {
        this(context, new ArrayList<>());
    }

    public MessageAdapter(@NonNull Context context, @NonNull List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    public int getLayoutLeft() {
        return layoutLeft;
    }

    public void setLayoutLeft(int layoutLeft) {
        this.layoutLeft = layoutLeft;
    }

    public int getLayoutRight() {
        return layoutRight;
    }

    public void setLayoutRight(int layoutRight) {
        this.layoutRight = layoutRight;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = viewType == USER_MESSAGE ? layoutRight : layoutLeft;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.message.setText(message.getText());
        holder.time.setText(message.getTimestamp() != null ? SDF.format(message.getTimestamp()) : "13:37");

        User sender = message.getSender();
        if(sender != null && sender.getPhotoURI() != null) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, sender.getUid());
            Picasso.with(context).load(contactUri).fit().transform(rounded).into(holder.thumb);
        } else {
            String name = sender != null ? sender.getName() : "A";
            String str = name.length() > 0 ? name.substring(0,1) : "A";
            TextDrawable drawable = TextDrawable.builder().buildRound(str, ColorGenerator.MATERIAL.getColor(name));
            holder.thumb.setImageDrawable(drawable);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        User owner = MessageService.getSingleton(context).getOwner();
        User sender = message.getSender();
        return owner == null || sender == null ? USER_MESSAGE :
                owner.equals(sender) ? USER_MESSAGE : RECIPIENT_MESSAGE;
    }

    public List<Message> getItems() {
        return messages;
    }
    public void setOnClickListener(@NonNull OnClickListener listener) {
        this.listener = listener;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView time;
        public TextView message;
        public ImageView thumb;

        public MessageViewHolder(View view) {
            super(view);
            view.setOnClickListener(v -> listener.onClick(getAdapterPosition()));
            this.time = (TextView)view.findViewById(R.id.time);
            this.message = (TextView)view.findViewById(R.id.message);
            this.thumb = (ImageView) view.findViewById(R.id.thumb);
        }
    }
}
