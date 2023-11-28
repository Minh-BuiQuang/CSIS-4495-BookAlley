using BookAlleyWebApi.Models;

namespace BookAlleyWebApi.RestModels
{
    public class MessageRequest
    {
        public long Id { get; set; }
        public required long ConversationId { get; set; }
        public required DateTimeOffset Timestamp { get; set; }
        public required string Content { get; set; }
        public required string Source { get; set; }
    }
}
