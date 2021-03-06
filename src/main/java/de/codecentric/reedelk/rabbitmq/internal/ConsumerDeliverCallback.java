package de.codecentric.reedelk.rabbitmq.internal;

import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import de.codecentric.reedelk.rabbitmq.component.RabbitMQConsumer;
import de.codecentric.reedelk.rabbitmq.internal.attribute.RabbitMQConsumerAttributes;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.message.MessageAttributes;
import de.codecentric.reedelk.runtime.api.message.MessageBuilder;
import de.codecentric.reedelk.runtime.api.message.content.MimeType;

import static de.codecentric.reedelk.runtime.api.commons.Preconditions.checkArgument;

abstract class ConsumerDeliverCallback implements DeliverCallback {

    private final MimeType consumedMessageMimeType;

    ConsumerDeliverCallback(MimeType consumedMessageMimeType) {
        checkArgument(consumedMessageMimeType != null, "consumedMessageMimeType");
        this.consumedMessageMimeType = consumedMessageMimeType;
    }

    @Override
    public void handle(String consumerTag, Delivery delivery) {
        // Message Content
        byte[] content = delivery.getBody();

        // Message Attributes
        MessageAttributes attributes = new RabbitMQConsumerAttributes(delivery);

        Message inboundMessage;

        // Convert the payload to a suitable type according to the mime type.
        if (String.class == consumedMessageMimeType.javaType()) {

            inboundMessage = MessageBuilder.get(RabbitMQConsumer.class)
                    .withString(new String(content), consumedMessageMimeType)
                    .attributes(attributes)
                    .build();
        } else {
            inboundMessage = MessageBuilder.get(RabbitMQConsumer.class)
                    .withBinary(content, consumedMessageMimeType)
                    .attributes(attributes)
                    .build();
        }

        // Notify event
        onEvent(inboundMessage, delivery);
    }

    protected abstract void onEvent(Message message, Delivery delivery);

}
