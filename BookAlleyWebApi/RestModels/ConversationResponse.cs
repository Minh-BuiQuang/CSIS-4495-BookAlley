using BookAlleyWebApi.Models;

namespace BookAlleyWebApi.RestModels
{
    public class ConversationResponse
    {
        public long Id { get; set; }
        public long PosterId { get; set; }
        public long FinderId { get; set; }  
        public string PosterName { get; set; }
        public string FinderName { get; set; }
        public ICollection<Message> Messages { get; set; }
    }
}
