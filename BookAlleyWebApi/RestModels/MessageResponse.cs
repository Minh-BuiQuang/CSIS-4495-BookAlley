namespace BookAlleyWebApi.Models
{
    public class MessageResponse
    {
        public long Id { get; set; }
        public long ConversationId { get; set; }
        public DateTimeOffset Timestamp { get; set; }
        public required string Content { get; set; }
        public required string Source { get; set; }
    }
}
