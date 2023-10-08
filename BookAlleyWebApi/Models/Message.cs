namespace BookAlleyWebApi.Models
{
    public class Message
    {
        public long Id { get; set; }
        public required Conversation Conversation { get; set; }
        public required DateTimeOffset Timestamp { get; set; }
        public required string Content { get; set; }
        public required MessageSource Source { get; set; }

        public enum MessageSource {
            system,
            poster,
            finder
        }
    }
}
